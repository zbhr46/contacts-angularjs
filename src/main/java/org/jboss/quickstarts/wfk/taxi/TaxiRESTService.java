/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.quickstarts.wfk.taxi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.NoSuchEntityException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;

/**
 * <p>This class exposes the functionality of {@link TaxiService} over HTTP endpoints as a RESTful resource via
 * JAX-RS.</p>
 *
 * <p>Full path for accessing the Taxi resource is rest/taxis .</p>
 *
 * <p>The resource accepts and produces JSON.</p>
 * 
 * @author Joshua Wilson
 * @see TaxiService
 * @see javax.ws.rs.core.Response
 */
/*
 * The Path annotation defines this as a REST Web Service using JAX-RS.
 * 
 * By placing the Consumes and Produces annotations at the class level the methods all default to JSON.  However, they 
 * can be overridden by adding the Consumes or Produces annotations to the individual method.
 * 
 * It is Stateless to "inform the container that this RESTful web service should also be treated as an EJB and allow 
 * transaction demarcation when accessing the database." - Antonio Goncalves
 * 
 */
@Path("/taxis")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Stateless
public class TaxiRESTService {
    @Inject
    private @Named("logger") Logger log;
    
    @Inject
    private TaxiService service;
    
    /**
     * <p>Search for and return all the Taxis.  They are sorted alphabetically by name.</p>
     * 
     * @return A Response containing a list of Taxis
     */
    @GET
    public Response retrieveAllTaxis() {
        List<Taxi> taxis = service.findAllOrderedByReg();
        return Response.ok(taxis).build();
    }

    /**
     * <p>Search for and return a Taxi identified by reg.<p/>
     *
     * <p>Path annotation includes very simple regex to differentiate between registrations and Ids.
     * <strong>DO NOT</strong> attempt to use this regex to validate registrations.</p>
     *
     *
     * @param reg The string parameter value provided as a Taxi's reg
     * @return A Response containing a single Taxi
     */
    @GET
    @Path("/{reg:[0-9]+}")
    public Response retrieveTaxisByReg(@PathParam("reg") String reg) {
        Taxi taxi;
        try {
            taxi = service.findByReg(reg);
        } catch (NoResultException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return Response.ok(taxi).build();
    }
    
    /**
     * <p>Search for and return a Taxi identified by id.</p>
     * 
     * @param id The long parameter value provided as a Taxi's id
     * @return A Response containing a single Taxi
     */
    @GET
    @Path("/{id:[0-9]+}")
    public Response retrieveTaxiById(@PathParam("id") long id) {
        Taxi taxi = service.findById(id);
        if (taxi == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        log.info("findById " + id + ": found Taxi = " + taxi.getNumSeats() + " " + taxi.getReg() + " "  + taxi.getId());
        
        return Response.ok(taxi).build();
    }

    /**
     * <p>Creates a new taxi from the values provided. Performs validation and will return a JAX-RS response with either 200 (ok)
     * or with a map of fields, and related errors.</p>
     * 
     * @param taxi The Taxi object, constructed automatically from JSON input, to be <i>created</i> via {@link TaxiService#create(Taxi)}
     * @return A Response indicating the outcome of the create operation
     */
    @SuppressWarnings("unused")
    @POST
    public Response createTaxi(Taxi taxi) {
        log.info("createTaxi started. Taxi = " +  taxi.getNumSeats() + " " + taxi.getReg() + " "  + taxi.getId());
        if (taxi == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        
        Response.ResponseBuilder builder = null;

        try {
            // Go add the new Taxi.
            service.create(taxi);

            // Create a "Resource Created" 201 Response and pass the taxi back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(taxi);
            
            log.info("createTaxi completed. Taxi = " +  taxi.getNumSeats() + " " + taxi.getReg() + " "  + taxi.getId());
        } catch (ConstraintViolationException ce) {
            log.info("ConstraintViolationException - " + ce.toString());
            // Handle bean validation issues
            builder = createViolationResponse(ce.getConstraintViolations());
        } catch (ValidationException e) {
            log.info("ValidationException - " + e.toString());
            // Handle the unique constrain violation
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("reg", "That reg  is already used, please use a unique reg ");
            builder = Response.status(Response.Status.CONFLICT).entity(responseObj);
        } catch (Exception e) {
            log.info("Exception - " + e.toString());
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("error", e.getMessage());
            builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
        }

        return builder.build();
    }

    /**
     * <p>Updates a taxi with the ID provided in the Taxi. Performs validation, and will return a JAX-RS response with either 200 ok,
     * or with a map of fields, and related errors.</p>
     * 
     * @param taxi The Taxi object, constructed automatically from JSON input, to be <i>updated</i> via {@link TaxiService#update(Taxi)}
     * @param id The long parameter value provided as the id of the Taxi to be updated
     * @return A Response indicating the outcome of the create operation
     */
    @PUT
    @Path("/{id:[0-9][0-9]*}")
    public Response updateTaxi(@PathParam("id") long id, Taxi taxi) {
        if (taxi == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        log.info("updateTaxi started. Taxi = " +  taxi.getNumSeats() + " " + taxi.getReg() + " " + taxi.getId());

        if (taxi.getId() != id) {
            // The client attempted to update the read-only Id. This is not permitted.
            Response response = Response.status(Response.Status.CONFLICT).entity("The taxi ID cannot be modified").build();
            throw new WebApplicationException(response);
        }
        if (service.findById(taxi.getId()) == null) {
            // Verify if the taxi exists. Return 404, if not present.
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        
        Response.ResponseBuilder builder = null;
        
        try {
            // Apply the changes the Taxi.
            service.update(taxi);

            // Create an OK Response and pass the taxi back in case it is needed.
            builder = Response.ok(taxi);

            log.info("updateTaxi completed. Taxi = " +  taxi.getNumSeats() + " " + taxi.getReg() + " " + taxi.getId());
        } catch (ConstraintViolationException ce) {
            log.info("ConstraintViolationException - " + ce.toString());
            // Handle bean validation issues
            builder = createViolationResponse(ce.getConstraintViolations());
        } catch (ValidationException e) {
            log.info("ValidationException - " + e.toString());
            // Handle the unique constrain violation
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("reg", "That reg is already used, please use a unique taxi reg");
            responseObj.put("error", "This is where errors are displayed that are not related to a specific field");
            responseObj.put("anotherError", "You can find this error message in /src/main/java/org/jboss/quickstarts/wfk/rest/TaxiRESTService.java line 242.");
            builder = Response.status(Response.Status.CONFLICT).entity(responseObj);
        } catch (Exception e) {
            log.info("Exception - " + e.toString());
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("error", e.getMessage());
            builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
        }

        return builder.build();
    }

    /**
     * <p>Deletes a taxi using the ID provided. If the ID is not present then nothing can be deleted.</p>
     *
     * <p>Will return a JAX-RS response with either 200 OK or with a map of fields, and related errors.</p>
     * 
     * @param id The Long parameter value provided as the id of the Taxi to be deleted
     * @return A Response indicating the outcome of the delete operation
     */
    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    public Response deleteTaxi(@PathParam("id") Long id) {
        log.info("deleteTaxi started. Taxi ID = " + id);
        Response.ResponseBuilder builder = null;

        try {
            Taxi taxi = service.findById(id);
            if (taxi != null) {
                service.delete(taxi);
            } else {
                log.info("TaxiRESTService - deleteTaxi - No taxi with matching ID was found so can't Delete.");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            builder = Response.noContent();
            log.info("deleteTaxi completed. Taxi = " + taxi.getNumSeats() + " " + taxi.getReg() + " "  + taxi.getId());
        } catch (Exception e) {
            log.info("Exception - " + e.toString());
            // Handle generic exceptions
            Map<String, String> responseObj = new HashMap<String, String>();
            responseObj.put("error", e.getMessage());
            builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
        }

        return builder.build();
    }
    
    /**
     * <p>Creates a JAX-RS "Bad Request" response including a map of all violation fields, and their message. This can be used
     * by calling client applications to display violations to users.<p/>
     * 
     * @param violations A Set of violations that need to be reported in the Response body
     * @return A Bad Request (400) Response containing all violation messages
     */
    private Response.ResponseBuilder createViolationResponse(Set<ConstraintViolation<?>> violations) {
        log.fine("Validation completed. violations found: " + violations.size());

        Map<String, String> responseObj = new HashMap<String, String>();

        for (ConstraintViolation<?> violation : violations) {
            responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
        }

        return Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    }


}
