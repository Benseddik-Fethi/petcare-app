package fr.benseddik.backend.service.impl;

import fr.benseddik.backend.domain.Appointment;
import fr.benseddik.backend.domain.Clinic;
import fr.benseddik.backend.domain.Pet;
import fr.benseddik.backend.domain.User;
import fr.benseddik.backend.domain.Vet;
import fr.benseddik.backend.dto.request.CreateAppointmentRequest;
import fr.benseddik.backend.dto.request.UpdateAppointmentRequest;
import fr.benseddik.backend.dto.response.AppointmentResponse;
import fr.benseddik.backend.dto.response.ClinicResponse;
import fr.benseddik.backend.dto.response.VetResponse;
import fr.benseddik.backend.exception.ResourceNotFoundException;
import fr.benseddik.backend.repository.*;
import fr.benseddik.backend.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des rendez-vous vétérinaires.
 *
 * 🛡️ SÉCURITÉ :
 * - Vérification que l'animal appartient bien à l'utilisateur
 * - Filtrage automatique par user_id
 * - Validation des entrées via DTOs
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final VetRepository vetRepository;
    private final ClinicRepository clinicRepository;

    @Override
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request, UUID userId) {
        log.debug("📅 Création d'un rendez-vous pour l'utilisateur: {}", userId);

        // 🛡️ SÉCURITÉ : Vérifier que l'animal appartient à l'utilisateur
        Pet pet = petRepository.findByIdAndOwnerId(request.petId(), userId)
                .orElseThrow(() -> new AccessDeniedException("Cet animal ne vous appartient pas"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        Appointment appointment = Appointment.builder()
                .date(request.date())
                .reason(request.reason())
                .status("scheduled")
                .durationMinutes(request.durationMinutes() != null ? request.durationMinutes() : 30)
                .user(user)
                .pet(pet)
                .build();

        // Relations optionnelles
        if (request.vetId() != null) {
            Vet vet = vetRepository.findById(request.vetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vétérinaire introuvable"));
            appointment.setVet(vet);
        }

        if (request.clinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.clinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Clinique introuvable"));
            appointment.setClinic(clinic);
        }

        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("✅ Rendez-vous créé: {} le {}", savedAppointment.getReason(), savedAppointment.getDate());

        return mapToResponse(savedAppointment);
    }

    @Override
    public List<AppointmentResponse> getAllAppointments(UUID userId) {
        log.debug("🔍 Récupération des rendez-vous de l'utilisateur: {}", userId);

        List<Appointment> appointments = appointmentRepository.findByUserIdOrderByDateDesc(userId);

        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> getUpcomingAppointments(UUID userId) {
        log.debug("🔍 Récupération des rendez-vous à venir pour: {}", userId);

        List<Appointment> appointments = appointmentRepository.findUpcomingByUserId(userId, LocalDateTime.now());

        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponse getAppointmentById(UUID appointmentId, UUID userId) {
        log.debug("🔍 Récupération du rendez-vous: {} pour: {}", appointmentId, userId);

        Appointment appointment = appointmentRepository.findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous introuvable ou accès non autorisé"));

        return mapToResponse(appointment);
    }

    @Override
    public List<AppointmentResponse> getAppointmentsByPet(UUID petId, UUID userId) {
        log.debug("🔍 Récupération des rendez-vous pour l'animal: {}", petId);

        // 🛡️ SÉCURITÉ : Vérifier que l'animal appartient à l'utilisateur
        petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new AccessDeniedException("Cet animal ne vous appartient pas"));

        List<Appointment> appointments = appointmentRepository.findByPetIdOrderByDateDesc(petId);

        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> getAppointmentsBetween(UUID userId, LocalDateTime start, LocalDateTime end) {
        log.debug("🔍 Récupération des rendez-vous entre {} et {}", start, end);

        List<Appointment> appointments = appointmentRepository.findByUserIdAndDateBetween(userId, start, end);

        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointment(UUID appointmentId, UpdateAppointmentRequest request, UUID userId) {
        log.debug("✏️ Mise à jour du rendez-vous: {}", appointmentId);

        // 🛡️ SÉCURITÉ : Vérification de propriété
        Appointment appointment = appointmentRepository.findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new AccessDeniedException("Accès non autorisé à ce rendez-vous"));

        // Mise à jour partielle
        if (request.date() != null) appointment.setDate(request.date());
        if (request.reason() != null) appointment.setReason(request.reason());
        if (request.status() != null) appointment.setStatus(request.status());
        if (request.notes() != null) appointment.setNotes(request.notes());
        if (request.durationMinutes() != null) appointment.setDurationMinutes(request.durationMinutes());

        // Mise à jour des relations optionnelles
        if (request.vetId() != null) {
            Vet vet = vetRepository.findById(request.vetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vétérinaire introuvable"));
            appointment.setVet(vet);
        }

        if (request.clinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.clinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Clinique introuvable"));
            appointment.setClinic(clinic);
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        log.info("✅ Rendez-vous mis à jour: {}", updatedAppointment.getId());

        return mapToResponse(updatedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(UUID appointmentId, UUID userId) {
        log.debug("❌ Annulation du rendez-vous: {}", appointmentId);

        // 🛡️ SÉCURITÉ : Vérification de propriété
        Appointment appointment = appointmentRepository.findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new AccessDeniedException("Accès non autorisé à ce rendez-vous"));

        appointment.cancel();
        Appointment cancelledAppointment = appointmentRepository.save(appointment);
        log.info("✅ Rendez-vous annulé: {}", cancelledAppointment.getId());

        return mapToResponse(cancelledAppointment);
    }

    @Override
    @Transactional
    public void deleteAppointment(UUID appointmentId, UUID userId) {
        log.debug("🗑️ Suppression du rendez-vous: {}", appointmentId);

        // 🛡️ SÉCURITÉ : Vérification de propriété
        Appointment appointment = appointmentRepository.findByIdAndUserId(appointmentId, userId)
                .orElseThrow(() -> new AccessDeniedException("Accès non autorisé à ce rendez-vous"));

        appointmentRepository.delete(appointment);
        log.info("✅ Rendez-vous supprimé: {}", appointmentId);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getDate(),
                appointment.getReason(),
                appointment.getStatus(),
                appointment.getNotes(),
                appointment.getDurationMinutes(),
                appointment.getPet().getId(),
                appointment.getPet().getName(),
                appointment.getVet() != null ? mapVetToResponse(appointment.getVet()) : null,
                appointment.getClinic() != null ? mapClinicToResponse(appointment.getClinic()) : null,
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }

    private VetResponse mapVetToResponse(Vet vet) {
        return new VetResponse(
                vet.getId(),
                vet.getFirstName(),
                vet.getLastName(),
                vet.getFullName(),
                vet.getSpecialty(),
                vet.getPhone(),
                vet.getEmail(),
                vet.getAvatar(),
                vet.getClinic() != null ? vet.getClinic().getId() : null,
                vet.getClinic() != null ? vet.getClinic().getName() : null,
                vet.getCreatedAt(),
                vet.getUpdatedAt()
        );
    }

    private ClinicResponse mapClinicToResponse(Clinic clinic) {
        return new ClinicResponse(
                clinic.getId(),
                clinic.getName(),
                clinic.getAddress(),
                clinic.getPhone(),
                clinic.getEmail(),
                clinic.getWebsite(),
                clinic.getOpeningHours(),
                clinic.getCreatedAt(),
                clinic.getUpdatedAt()
        );
    }
}
