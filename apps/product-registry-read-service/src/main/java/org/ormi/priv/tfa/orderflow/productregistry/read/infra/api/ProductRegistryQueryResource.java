// Méthode utilitaire
private boolean isValidUUID(String id) {
    try {
        UUID.fromString(id);
        return true;
    } catch (IllegalArgumentException e) {
        return false;
    }
}

// Dans searchProducts()
if (page < 0) {
    throw new BadRequestException("page must be >= 0");
}
if (size < 1 || size > 100) {
    throw new BadRequestException("size must be between 1 and 100");
}
