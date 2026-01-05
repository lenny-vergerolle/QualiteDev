package org.ormi.priv.tfa.orderflow.productregistry.read.application;

import java.util.Optional;

import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductStreamElementDto;
import org.ormi.priv.tfa.orderflow.cqrs.Projector.ProjectionResult;
import org.ormi.priv.tfa.orderflow.kernel.Product;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1Envelope;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.kernel.product.persistence.ProductViewRepository;
import org.ormi.priv.tfa.orderflow.kernel.product.views.ProductView;
import org.ormi.priv.tfa.orderflow.productregistry.read.projection.ProductViewProjector;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.resource.spi.IllegalStateException;
import jakarta.transaction.Transactional;

/**
 * Dispatcheur de projections CQRS pour les événements de produits.
 *
 * <p>Orchestre la projection des événements {@link ProductEventV1Envelope}
 * vers des vues de lecture {@link ProductView} en suivant le pattern CQRS.
 * Gère le cycle complet : récupération → projection → persistance → diffusion.</p>
 *
 * <h3>Cycle de projection</h3>
 * <ol>
 *   <li>Récupération de la vue courante (optimistic concurrency)</li>
 *   <li>Projection via {@link ProductViewProjector}</li>
 *   <li>Persistance transactionnelle si succès</li>
 *   <li>Diffusion via {@link ProductEventBroadcaster}</li>
 * </ol>
 */
@ApplicationScoped
public class ProjectionDispatcher {
    
    /** Type d'agrégat géré (Product.class.getSimpleName()) */
    private static final String PRODUCT_AGGREGATE_TYPE = Product.class.getSimpleName();

    private final Instance<ProductViewProjector> productViewProjector;
    private final ProductViewRepository productViewRepository;
    private final ProductEventBroadcaster productEventBroadcaster;

    /**
     * Constructeur CDI avec injection de dépendances.
     *
     * @param productViewProjector instance dynamique du projecteur (CDI Instance)
     * @param productViewRepository repository des vues de lecture
     * @param productEventBroadcaster broadcaster pour diffuser les projections
     */
    @Inject
    public ProjectionDispatcher(
            Instance<ProductViewProjector> productViewProjector,
            ProductViewRepository productViewRepository,
            ProductEventBroadcaster productEventBroadcaster) {
        this.productViewProjector = productViewProjector;
        this.productViewRepository = productViewRepository;
        this.productEventBroadcaster = productEventBroadcaster;
    }

    /**
     * Dispatche et projette un événement produit de manière transactionnelle.
     *
     * <p>⚠️ Transactionnelle : atomicité garantie (projection + persistance + diffusion).</p>
     *
     * <h3>Logique détaillée</h3>
     * <ul>
     *   <li>Filtre les événements Product uniquement</li>
     *   <li>Charge la vue courante (ou Optional.empty())</li>
     *   <li>Projette via CDI Instance (permet plusieurs projecteurs)</li>
     *   <li>Persiste et diffuse UNIQUEMENT si succès</li>
     * </ul>
     *
     * <p><b>TODOs en cours :</b></p>
     * <ul>
     *   <li>Logger les échecs de projection (cas anormal)</li>
     *   <li>Logger les NoOp (ordering cassé temporairement)</li>
     * </ul>
     *
     * @param event l'enveloppe d'événement à projeter
     * @return résultat de la projection ({@link ProjectionResult})
     * @throws IllegalStateException si type d'agrégat inconnu
     */
    @Transactional
    public ProjectionResult<ProductView> dispatch(ProductEventV1Envelope<?> event) 
            throws IllegalStateException {
        
        if (event.aggregateType().equals(PRODUCT_AGGREGATE_TYPE)) {
            final Optional<ProductView> currentView = productViewRepository
                    .findById(new ProductId(event.aggregateId()));
            
            final ProjectionResult<ProductView> result = 
                productViewProjector.get().project(currentView, event);
            
            if (result.isFailure()) {
                // TODO: Hey ! Log the failure. It is not a normal case
                // log.error("Projection failed for {}: {}", event.aggregateId(), result.getError());
                return result;
            }
            
            if (result.isNoOp()) {
                // TODO: Log info. It may happen if ordering is temporarily broken
                // log.info("Projection NoOp for {} (possible ordering issue)", event.aggregateId());
            }
            
            if (result.isSuccess()) {
                productViewRepository.save(result.getProjection());
                productEventBroadcaster.broadcast(new ProductStreamElementDto(
                    event.event().eventType(),
                    event.aggregateId().toString(),
                    event.timestamp()
                ));
            }
            return result;
        }
        throw new IllegalStateException("Unmatched aggregate type: " + event.aggregateType());
    }
}
