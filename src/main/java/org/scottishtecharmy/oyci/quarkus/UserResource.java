package org.scottishtecharmy.oyci.quarkus;

import org.mindrot.jbcrypt.BCrypt;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;
import org.scottishtecharmy.oyci.quarkus.model.ContactDetail;
import org.scottishtecharmy.oyci.quarkus.model.Role;
import org.scottishtecharmy.oyci.quarkus.model.User;
import org.scottishtecharmy.oyci.quarkus.repository.ContactDetailRepository;
import org.scottishtecharmy.oyci.quarkus.repository.RoleRepository;
import org.scottishtecharmy.oyci.quarkus.repository.UserRepository;
import org.scottishtecharmy.oyci.quarkus.request.LoginRequest;
import org.scottishtecharmy.oyci.quarkus.request.SignupRequest;
import org.scottishtecharmy.oyci.quarkus.response.AuthResponse;
import org.scottishtecharmy.oyci.quarkus.response.RoleResponse;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    ContactDetailRepository contactDetailRepository;

    /**
     * Get all roles for picklist/dropdown
     * GET /auth/roles
     */
    @GET
    @Path("/roles")
    public List<RoleResponse> getRoles() {
        return roleRepository.listAll()
                .stream()
                .map(RoleResponse::from)
                .collect(Collectors.toList());
    }

    @POST
    @Path("/signup")
    @Transactional
    public Response signup(@Valid SignupRequest request) {

        // Check if email is already registered
        if (contactDetailRepository.findByPrimaryEmail(request.getEmail()).isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new AuthResponse(null, request.getEmail(), null, null, "Email already registered"))
                    .build();
        }

        // Look up role
        Role role = roleRepository.findByRoleId(request.getRoleId());
        if (role == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new AuthResponse(null, request.getEmail(), null, null, "Invalid role ID"))
                    .build();
        }

        // Create ContactDetail — DB generates contact_detail_id
        ContactDetail contactDetail = new ContactDetail();
        contactDetail.setPrimaryEmail(request.getEmail());
        contactDetail.setRecordCreatedBy("system");
        contactDetail.setRecordUpdatedBy("system");
        contactDetailRepository.persist(contactDetail);

        // Create new user with hashed password — DB generates user_id
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setContactDetail(contactDetail);
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(10)));
        user.setRoleId(role);
        user.setIsActive(true);
        user.setRecordCreatedBy("system");
        user.setRecordUpdatedBy("system");
        userRepository.persist(user);

        String fullName = user.getFirstName() + " " + user.getLastName();
        return Response.status(Response.Status.CREATED)
                .entity(new AuthResponse(user.getUserId(), request.getEmail(), fullName, role.getName(), "Signup successful"))
                .build();
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {

        // Find user by username (matching against email)
        User user = userRepository.findByEmail(request.getUsername());
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new AuthResponse(null, request.getUsername(), null, null, "Invalid username or password"))
                    .build();
        }

        // Check if user is active
        if (user.getIsActive() != null && !user.getIsActive()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new AuthResponse(null, request.getUsername(), null, null, "User account is inactive"))
                    .build();
        }

        // Verify password against stored BCrypt hash
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new AuthResponse(null, request.getUsername(), null, null, "Invalid username or password"))
                    .build();
        }

        // Validate role if provided in request
        String roleName = user.getRoleId() != null ? user.getRoleId().getName() : null;
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            if (roleName == null || !roleName.equalsIgnoreCase(request.getRole())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(new AuthResponse(null, request.getUsername(), null, roleName, "User does not have the required role"))
                        .build();
            }
        }

        // Build full name with middle name if present
        String fullName = user.getFirstName();
        if (user.getMiddleName() != null && !user.getMiddleName().trim().isEmpty()) {
            fullName += " " + user.getMiddleName();
        }
        fullName += " " + user.getLastName();

        String email = user.getContactDetail() != null ? user.getContactDetail().getPrimaryEmail() : null;

        return Response.ok(

                new AuthResponse(user.getUserId(), email, fullName, roleName, "Login successful")
        ).build();
    }

}

