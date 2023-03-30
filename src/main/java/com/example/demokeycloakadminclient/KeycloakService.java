package com.example.demokeycloakadminclient;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Created by phuongtran on 6/6/17.
 */
@Service
public class KeycloakService {

    @Value("${keycloak.serverUrl}")
    private String SERVER_URL;

    @Value("${keycloak.realm}")
    private String REALM;

    @Value("${keycloak.username}")
    private String USERNAME;

    @Value("${keycloak.password}")
    private String PASSWORD;

    @Value("${keycloak.clientId}")
    private String CLIENT_ID;

    Keycloak keycloak = KeycloakBuilder.builder()
            .serverUrl("http://localhost:8180/auth")
            .grantType(OAuth2Constants.PASSWORD)
            .realm("master")
            .clientId("admin-cli")
            .username("admin")
            .password("admin")
            .resteasyClient(
                    new ResteasyClientBuilder()
                            .connectionPoolSize(10).build()
            ).build();

    /**
     * By default KeyCloak REST API doesn't allow to create account with credential type is PASSWORD,
     * it means after created account, need an extra step to make it works, it's RESET PASSWORD
     * @param username
     * @param password
     * @return
     */
    public int createAccount(final String username, final String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setFirstName("First Name");
        user.setLastName("Last Name");
        user.singleAttribute("customAttribute", "customAttribute");
        user.setCredentials(Arrays.asList(credential));
        javax.ws.rs.core.Response response = keycloak.realm("javamicroapp").users().create(user);
        final int status = response.getStatus();
        final String createdId = KeyCloakUtil.getCreatedId(response);
        // Reset password
        RolesRepresentation rolesRepresentation = new RolesRepresentation();
        rolesRepresentation.getRealm();
        CredentialRepresentation newCredential = new CredentialRepresentation();
        UserResource userResource = keycloak.realm("javamicroapp").users().get(createdId);
        RealmResource realmResource = keycloak.realm("javamicroapp");
        newCredential.setType(CredentialRepresentation.PASSWORD);
        newCredential.setValue(password);
        newCredential.setTemporary(false);
        userResource.resetPassword(newCredential);
        RoleRepresentation testerRealmRole = realmResource.roles()//
                .get("admin").toRepresentation();
//
//        // Assign realm role tester to user
        userResource.roles().realmLevel() //
                .add(Arrays.asList(testerRealmRole));
//
//        // Get client
        ClientRepresentation app1Client = realmResource.clients() //
                .findByClientId("service1").get(0);
//
//        // Get client level role (requires view-clients role)
        RoleRepresentation userClientRole = realmResource.clients().get(app1Client.getId()) //
                .roles().get("user").toRepresentation();
//
//        // Assign client level role to user
        userResource.roles() //
                .clientLevel(app1Client.getId()).add(Arrays.asList(userClientRole));
        return HttpStatus.CREATED.value();
    }

}
