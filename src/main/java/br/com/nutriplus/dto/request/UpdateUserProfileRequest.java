package br.com.nutriplus.dto.request;

public record UpdateUserProfileRequest(
        String name,
        String photoUrl
) {
}
