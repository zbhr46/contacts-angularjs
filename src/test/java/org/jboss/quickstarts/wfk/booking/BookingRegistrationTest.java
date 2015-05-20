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
package org.jboss.quickstarts.wfk.booking;

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
import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerRESTService;
import org.jboss.quickstarts.wfk.customer.CustomerRegistrationTest;
import org.jboss.quickstarts.wfk.customer.CustomerRepository;
import org.jboss.quickstarts.wfk.customer.CustomerService;
import org.jboss.quickstarts.wfk.customer.CustomerValidator;
import org.jboss.quickstarts.wfk.taxi.Taxi;
import org.jboss.quickstarts.wfk.taxi.TaxiRESTService;
import org.jboss.quickstarts.wfk.taxi.TaxiRepository;
import org.jboss.quickstarts.wfk.taxi.TaxiService;
import org.jboss.quickstarts.wfk.taxi.TaxiValidator;
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
 * Customer creation functionality
 * (see {@link CustomerRESTService#createCustomer(Customer) createCustomer(Customer)}).<p/>
 *
 * 
 * @author balunasj
 * @author Andrew Mathews, adapted from code by Joshua Wilson
 * @see CustomerRESTService
 */
@RunWith(Arquillian.class)
public class BookingRegistrationTest {

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
        //HttpComponents and org.JSON are required by CustomerService
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml").resolve(
                "org.apache.httpcomponents:httpclient:4.3.2",
                "org.json:json:20140107"
        ).withTransitivity().asFile();

        Archive<?> archive = ShrinkWrap
            .create(WebArchive.class, "test.war")
            .addClasses(Customer.class, 
                        CustomerRESTService.class, 
                        CustomerRepository.class, 
                        CustomerValidator.class, 
                        CustomerService.class, 
                        Taxi.class, 
                        TaxiRESTService.class, 
                        TaxiRepository.class, 
                        TaxiValidator.class, 
                        TaxiService.class,
                        Booking.class, 
                        BookingRESTService.class, 
                        BookingRepository.class, 
                        BookingValidator.class, 
                        BookingService.class,
                        Resources.class)
            .addAsLibraries(libs)
            .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
            .addAsWebInfResource("arquillian-ds.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        
        return archive;
    }

    @Inject
    BookingRESTService bookingRESTService;
    @Inject
    CustomerRESTService customerRESTService;
    @Inject
    CustomerService customerService;
    @Inject
    TaxiRESTService taxiRESTService;
    @Inject
    TaxiService taxiService;
    @Inject
    @Named("logger") Logger log;

    //Set millis 498484800000 from 1985-10-10T12:00:00.000Z
   	private Date date = new Date(2016,01,02);
    
    //Create taxi and customer objects to be used in the tests
    Taxi taxi =  createTaxiInstance(7,"p799sna");
    Taxi taxi1 = createTaxiInstance(3,"p799snb");
    Taxi taxi2 = createTaxiInstance(5,"p799snc");
    Taxi taxi3 = createTaxiInstance(4,"p523snc");
    Taxi taxi4 = createTaxiInstance(5,"p732snc");
    Taxi taxi5 = createTaxiInstance(4,"p598snc");
    
    Customer customer = createCustomerInstance("Bob", "bob@mailinator.com", "01225593234");
    Customer customer1= createCustomerInstance("Will","will@mailinator.com","02534637492");
    Customer customer2= createCustomerInstance("Dom","dom@mailinator.com","02358745849");
    Customer customer3= createCustomerInstance("Andy","andy@mailinator.com","05398294839");
    Customer customer4= createCustomerInstance("Steve","steve@mailinator.com","02847395849");
    Customer customer5= createCustomerInstance("Don","donny@mailinator.com","05394758639");
    
    //Test registering a booking
    @SuppressWarnings("unchecked")
    @Test
    @InSequence(1)
    public void testRegister() throws Exception {
        
    	customerRESTService.createCustomer(customer);
        taxiRESTService.createTaxi(taxi);
        
        Booking booking = createBookingInstance(customer.getId(),taxi.getId(),date);
        Response response = bookingRESTService.createBooking(booking);
        
        assertEquals("Unexpected response status", 201, response.getStatus());
        log.info(" New booking was persisted and returned status " + response.getStatus());
    }

    
    //Test registering an invalid booking where a customer and taxi ID that don't exist are used
    @SuppressWarnings("unchecked")
    @Test
    @InSequence(2)
    public void testInvalidRegister() throws Exception {
       
        Booking booking=createBookingInstance((long)99999,(long)99999,date);
        Response response = bookingRESTService.createBooking(booking);
        
        //Check that it's a bad request (error 400)
        assertEquals("Unexpected response status", 400, response.getStatus());
       
        assertEquals("Unexpected response.getEntity(). It contains " + response.getEntity(), 1,
            ((Map<String, String>) response.getEntity()).size());
        log.info("Invalid booking register attempt failed with return code " + response.getStatus());
    }

	// Tests that registering a duplicate booking isn't allowed
	// i.e. registering two booking with same date and taxi combination
    @SuppressWarnings("unchecked")
    @Test
    @InSequence(3)
    public void testDuplicateBooking() throws Exception {
        
    	// Create two customers and a taxi object
    	customerRESTService.createCustomer(customer2);
    	customerRESTService.createCustomer(customer3);
        taxiRESTService.createTaxi(taxi2);
        
        // Register a booking
        Booking booking1 = createBookingInstance(customer2.getId(),taxi2.getId(),date);
        Response response1 = bookingRESTService.createBooking(booking1);
        
      	// Register a second booking with same date and taxi
        Booking booking2 = createBookingInstance(customer3.getId(),taxi2.getId(),date);
        Response response2 = bookingRESTService.createBooking(booking2);

        assertEquals("Unexpected response status",201, response1.getStatus());
    	assertEquals("Unexpected response status",400, response2.getStatus());
        assertEquals("Unexpected response.getEntity(). It contains" + response2.getEntity(), 1,
            ((Map<String, String>) response2.getEntity()).size());
        log.info("Duplicate customer register attempt failed with return code " + response2.getStatus());
    }
    
    
    @SuppressWarnings("unchecked")
    @Test
    @InSequence(4)
    public void testRetrieveAllBookings() throws Exception {
        
        Booking booking1 = createBookingInstance((long)10001,(long)10001, date);
        bookingRESTService.createBooking(booking1);
        
        Booking booking2 = createBookingInstance((long)10002,(long)10002, date);
        bookingRESTService.createBooking(booking2);
        
        Response response = bookingRESTService.retrieveAllBookings();

        assertEquals("Unexpected response status", 200, response.getStatus());
        log.info("List of all bookings was persisted and returned status " + response.getStatus());
    }
   
   @SuppressWarnings("unchecked") 
   @Test
   @InSequence(5)
   public void testDeleteBooking() throws Exception {
   
   		customerRESTService.createCustomer(customer4);
        taxiRESTService.createTaxi(taxi3);
        
        Booking booking3 = createBookingInstance(customer4.getId(),taxi3.getId(),date);
        Response response = bookingRESTService.deleteBooking(booking3.getId()); 
        
        assertEquals("Unexpected response status", 400, response.getStatus());
        log.info(" Booking was deleted and returned status " + response.getStatus());   
        
    }

    /**
     * <p>A utility method to construct a {@link org.jboss.quickstarts.wfk.Booking.Booking Booking} object for use in
     * testing. This object is not persisted.</p>
     *
     * @param customerID The first name of the Customer being created
     * @param taxiID The last name of the Customer being created
     * @param booking_date The booking date 
     * @return The Booking object create
     */
    
    
    Customer createCustomerInstance(String name,String email, String phone) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhoneNumber(phone);
        
        return customer;
    }
    
    Taxi createTaxiInstance(Integer numSeats, String reg) {
        Taxi taxi = new Taxi();
        taxi.setNumSeats(numSeats);
        taxi.setReg(reg);
      
        return taxi;
    }

    
    Booking createBookingInstance(Long customerID, Long taxiID, Date bookingdate) {
        
    	
    	Booking booking = new Booking(); 
    	      
        //get the customer and taxi by Their ID
    	Customer customer = customerService.findById(customerID);
        Taxi taxi = taxiService.findById(taxiID);
        
        //initialize the booking 
        booking.setCustomer(customer);
        booking.setTaxi(taxi);
        booking.setBookingDate(bookingdate);
        return booking;
    }
}
