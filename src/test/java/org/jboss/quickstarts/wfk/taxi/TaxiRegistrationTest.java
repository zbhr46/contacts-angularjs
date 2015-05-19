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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;

import org.jboss.quickstarts.wfk.util.Resources;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>A suite of tests, run with {@link org.jboss.arquillian Arquillian} to test the JAX-RS endpoint for
 * Taxi creation functionality
 * (see {@link TaxiRESTService#createTaxi(Taxi) createTaxi(Taxi)}).<p/>
 *
 * 
 * @author balunasj
 * @author Joshua Wilson
 * @see TaxiRESTService
 */
@RunWith(Arquillian.class)
public class TaxiRegistrationTest {

    /**
     * <p>Compiles an Archive using Shrinkwrap, containing those external dependencies necessary to run the tests.</p>
     *
     * <p>Note: This code will be needed at the start of each Arquillian test, but should not need to be edited, except
     * to pass *.class values to .addClasses(...) which are appropriate to the functionality you are trying to test.</p>
     *
     * @return Micro test war to be deployed and executed.
     */
    @Deployment
    public static Archive<?> createTestArchive() {
        //HttpComponents and org.JSON are required by TaxiService
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").resolve(
                "org.apache.httpcomponents:httpclient:4.3.2",
                "org.json:json:20140107"
        ).withTransitivity().asFile();

        Archive<?> archive = ShrinkWrap
            .create(WebArchive.class, "test.war")
            .addClasses(Taxi.class, 
                        TaxiRESTService.class, 
                        TaxiRepository.class, 
                        TaxiValidator.class, 
                        TaxiService.class, 
                        Resources.class)
            .addAsLibraries(libs)
            .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
            .addAsWebInfResource("arquillian-ds.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        
        return archive;
    }

    @Inject
    TaxiRESTService taxiRESTService;
    
    @Inject
    @Named("logger") Logger log;


    @Test
    @InSequence(1)
    public void testRegister() throws Exception {
        Taxi taxi = createTaxiInstance(7, "p799sna");
        Response response = taxiRESTService.createTaxi(taxi);

        assertEquals("Unexpected response status", 201, response.getStatus());
        log.info(" New taxi was persisted and returned status " + response.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    @InSequence(2)
    public void testInvalidRegister() throws Exception {
        Taxi taxi = createTaxiInstance(30, "");
        Response response = taxiRESTService.createTaxi(taxi);

        assertEquals("Unexpected response status", 400, response.getStatus());
        assertNotNull("response.getEntity() should not be null", response.getEntity());
        assertEquals("Unexpected response.getEntity(). It contains " + response.getEntity(), 2,
            ((Map<String, String>) response.getEntity()).size());
        log.info("Invalid taxi register attempt failed with return code " + response.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    @InSequence(3)
    public void testDuplicateReg() throws Exception {
        // Register an initial user
        Taxi taxi = createTaxiInstance(8, "p799snb");
        taxiRESTService.createTaxi(taxi);

        // Register a different user with the same email
        Taxi anotherTaxi = createTaxiInstance(9, "p799snb");
        Response response = taxiRESTService.createTaxi(anotherTaxi);

        assertEquals("Unexpected response status", 409, response.getStatus());
        assertNotNull("response.getEntity() should not be null", response.getEntity());
        assertEquals("Unexpected response.getEntity(). It contains" + response.getEntity(), 1,
            ((Map<String, String>) response.getEntity()).size());
        log.info("Duplicate taxi register attempt failed with return code " + response.getStatus());
    }
    
    
    @Test
    @InSequence(4)
    public void testRetrieveAllTaxis() throws Exception {
        Taxi taxi = createTaxiInstance(7, "j799snh");
        taxiRESTService.createTaxi(taxi);
        
        Taxi anotherTaxi = createTaxiInstance(6, "k798snf");
        taxiRESTService.createTaxi(anotherTaxi);
        
        Response response = taxiRESTService.retrieveAllTaxis();

        assertEquals("Unexpected response status", 200, response.getStatus());
        log.info(" List of all taxis was persisted and returned status " + response.getStatus());
    }


    /**
     * <p>A utility method to construct a {@link org.jboss.quickstarts.wfk.Booking.Taxi Taxi} object for use in
     * testing. This object is not persisted.</p>
     *
     * @param firstName The first name of the Taxi being created
     * @param lastName  The last name of the Taxi being created
     * @param email     The email address of the Taxi being created
     * @param phone     The phone number of the Taxi being created
     * @param birthDate The birth date of the Taxi being created
     * @return The Taxi object create
     */
      Taxi createTaxiInstance(Integer numSeats, String reg) {
        Taxi taxi = new Taxi();
        taxi.setNumSeats(numSeats);
        taxi.setReg(reg);
      
        return taxi;
    }
}
