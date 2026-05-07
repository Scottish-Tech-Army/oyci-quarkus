package org.scottishtecharmy.oyci.quarkus.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.scottishtecharmy.oyci.quarkus.entity.Tag;
import org.scottishtecharmy.oyci.quarkus.service.TagService;

import java.util.List;

@Path("/tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class TagResource {

    @Inject
    TagService tagService;

    @GET
    @RolesAllowed({"ADMIN", "STAFF"})
    public List<Tag> list() {
        return tagService.listAll();
    }

    @POST
    public Response create(Tag tag) {
        Tag created = tagService.create(tag);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Tag update(@PathParam("id") Long id, Tag tag) {
        return tagService.update(id, tag);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        tagService.delete(id);
        return Response.noContent().build();
    }
}

