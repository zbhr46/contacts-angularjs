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
 * @author Joshua Wilson
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
   
    private Date date = new Date(2015,12,12);
    
    //Create customer and taxi objects to use in the tests
    Customer customer = createCustomerInstance("Jack", "jack@mailinator.com", "02225551234");
    Customer customer1= createCustomerInstance("Paul","P.C@maid.com","02589631475");
    Customer customer2= createCustomerInstance("Naul","PUIC@magid.com","03698745219");
    Taxi taxi = createTaxiInstance(7,  "p799sna");
    Taxi taxi1= createTaxiInstance(3,"p799snb");
    Taxi taxi2= createTaxiInstance(5,"p799snc");
    
    
    
    
    @Test
    @InSequence(1)
    public void testRegister() throws Exception {
        
    	 customerRESTService.createCustomer(customer);
         taxiRESTService.createTaxi(taxi);
        
        
        
        Booking booking=createBookingInstance(customer.getId(),taxi.getId(),date);
        Response response3 = bookingRESTService.createBooking(booking);
        //CORRECT  
        assertEquals("Unexpected response status", 201, response3.getStatus());
        log.info(" New booking was persisted and returned status " + response3.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    @InSequence(2)
    public void testInvalidRegister() throws Exception {
       
        
        //register an invalid registraton 
        Booking booking=createBookingInstance((long)23423,(long)423420,date);
        Response response2 = bookingRESTService.createBooking(booking);
        
  
        //bad request
        assertEquals("Unexpected response status", 400, response2.getStatus());
       
        assertEquals("Unexpected response.getEntity(). It contains " + response2.getEntity(), 1,
            ((Map<String, String>) response2.getEntity()).size());
        log.info("Invalid booking register attempt failed with return code " + response2.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    @InSequence(3)
    public void testDuplicateBooking() throws Exception {
        
    	customerRESTService.createCustomer(customer2);
    	customerRESTService.createCustomer(customer1);
        taxiRESTService.createTaxi(taxi2);
        //taxiRESTService.createTaxi(taxi1);
        //Register a booking
        Booking booking=createBookingInstance(customer1.getId(),taxi2.getId(),date);
        Response booking_response1 = bookingRESTService.createBooking(booking);
        
      
        
        // Register a different booking with same date and taxi
        Booking anotherBooking = createBookingInstance(customer2.getId(),taxi2.getId(),date);
        Response booking_response2 = bookingRESTService.createBooking(anotherBooking);

        assertEquals("Unexpected response status",201,booking_response1.getStatus());
    //    assertEquals("Unexpected response status",409, booking_response2.getStatus());
        assertEquals("Unexpected response.getEntity(). It contains" + booking_response2.getEntity(), 1,
            ((Map<String, String>) booking_response2.getEntity()).size());
        log.info("Duplicate customer register attempt failed with return code " + booking_response2.getStatus());
    }
    
    
    @Test
    @InSequence(4)
    public void testRetrieveAllBookings() throws Exception {
        
        Booking booking = createBookingInstance((long)10001,(long)10001, date);
        bookingRESTService.createBooking(booking);
        
        Booking anotherBooking = createBookingInstance((long)10002,(long)10002, date);
        bookingRESTService.createBooking(anotherBooking);
        
        Response response = bookingRESTService.retrieveAllBookings();

        assertEquals("Unexpected response status", 200, response.getStatus());
        log.info(" List of all bookings was persisted and returned status " + response.getStatus());
    }
    
   // @Test
   // @InSequence(5)
   // public void testDeleteBooking() throws Exception {
        
   //     Customer customer12 = createCustomerInstance("Jim", "jimmy@mailinator.com", "02225586234");
   //     Taxi taxi12 = createTaxiInstance(7,  "p799sna");
        
   //     customerRESTService.createCustomer(customer12);
   //     taxiRESTService.createTaxi(taxi12);
        
        
        
     //   Booking booking12 = createBookingInstance(customer12.getId(),taxi12.getId(),date);
     //   Response response = bookingRESTService.deleteBooking(booking12.getId());

     //   assertEquals("Unexpected response status", 204, response.getStatus());
     //   log.info("Booking deleted and returned status " + response.getStatus());
    //}

    /**
     * <p>A utility method to construct a {@link org.jboss.quickstarts.wfk.Booking.Booking Booking} object for use in
     * testing. This object is not persisted.</p>
     *
     * @param customerID The first name of the Customer being created
     * @param taxiID The last name of the Customer being created
     * @param booking_date The booking date 
     * @return The Booking object create
     */
    
    
    Customer createCustomerInstance(String Name,String email, String phone) {
        Customer customer = new Customer();
        customer.setName(Name);
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

    
    Booking createBookingInstance(Long customerID, Long taxiID, Date booking_date) {
        
    	
    	Booking booking = new Booking();       
        //get the customer and taxi by Their ID
    	Customer customer =customerService.findById(customerID);
        Taxi taxi = taxiService.findById(taxiID);
        
        //initialize the booking 
        booking.setCustomer(customer);
        booking.setTaxi(taxi);
        booking.setBookingDate(booking_date);
        return booking;
    }
}
