package com.impala.campala.model

/**
 * This stores all the information shown to the admin when viewing profiles. Includes information on what profile is active and their details
 *
 * @param userId ID of the user
 * @param name Name of the user
 * @param email Email of the user
 * @param phoneNumber Phone number of the user
 * @param imageId ID of the user's profile picture
 * @param hasAnActiveEntrantProfile Whether the user has an active entrant profile
 * @param hasAnActiveCampsiteOwnerProfile Whether the user has an active campsite owner profile
 */
class AdminProfileModel(
    val userId: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val imageId: String,
    val hasAnActiveEntrantProfile: Boolean,
    val hasAnActiveCampsiteOwnerProfile: Boolean
) {
}