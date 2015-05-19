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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.customer.CustomerRepository;
import org.jboss.quickstarts.wfk.taxi.Taxi;
import org.jboss.quickstarts.wfk.taxi.TaxiRepository;

/**
 * <p>This class provides methods to check Booking objects against arbitrary requirements.</p>
 * 
 * @author Joshua Wilson
 * @see Booking
 * @see BookingRepository
 * @see javax.validation.Validator
 */
public class BookingValidator {
    @Inject
    private Validator validator;

    @Inject
    private BookingRepository crud;
    @Inject
    private CustomerRepository CustomerCrud;
    @Inject
    private TaxiRepository TaxiCrud;
    /**
     * <p>Validates the given Booking object and throws validation exceptions based on the type of error. If the error is standard
     * bean validation errors then it will throw a ConstraintValidationException with the set of the constraints violated.<p/>
     *
     *
     * <p>If the error is caused because an existing booking with the same email is registered it throws a regular validation
     * exception so that it can be interpreted separately.</p>
     *
     * 
     * @param booking The Booking object to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If booking with the same email already exists
     */
    void validateBooking(Booking booking) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Booking>> violations = validator.validate(booking);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the email BOOKING
        if (BookingAlreadyExists(booking.getTaxiId(),booking.getBookingDate(), booking.getId())) {
            throw new ValidationException("Unique booking Violation");
        }
        //check the if the  customer with a ID exist
       if(Customer_Not_Exists(booking.getCustomer().getId()))
       {
    	   throw new ValidationException("Customer does not exist");
       }
       
       if(Taxi_Not_Exists(booking.getTaxi().getId()))
       {
    	  throw new ValidationException("Taxi does not exist");   
       }
       
    }

    /**
     * <p>Checks if a booking with the same taxi and date is already registered. This is the only way to easily capture the
     * "@UniqueConstraint(columnNames = "id")" constraint from the Booking class.</p>
     * 
     * <p>Since Update will being using a taxi and date that is already in the database we need to make sure that it is the taxi
     * from the record being updated.</p>
     * 
     * @param taxiId The taxi to check is unique
     * @param id The user id to check the taxi against if it was found
     * @return boolean which represents whether the taxi and date was found, and if so if it belongs to the user with id
     */
    
    
    //check the uniqueness of Taxi and Date 
    boolean BookingAlreadyExists(long taxiId,Date date, Long id) {
        List<Booking> booking = null;
        Booking bookingWithID = null;
        try {
            booking = crud.findByTaxi(taxiId);
        } catch (NoResultException e) {
            // ignore
        }

         
            try {
            	
            	for(Booking book:booking)
            	{
                bookingWithID = crud.findById(id);
                if (book.getTaxiId()==taxiId && book.getBookingDate()==date) {
                    bookingWithID=book;
                }
            	}
            } catch (NoResultException e) {
                // ignore
            }
        
        return bookingWithID!=null;
    }
    
    //check if the customer exists 
    boolean  Customer_Not_Exists(Long id)
    {
    	Customer customer=null;
    	
        try{
            customer=CustomerCrud.findById(id);    
         
        }catch(NullPointerException e){
          //ignore	
        }
         
        
        return  customer==null;
    	
    }
   // check if the taxi exists
    boolean  Taxi_Not_Exists(Long id)
    {
    	Taxi taxi=null;
    	
        try{
            taxi=TaxiCrud.findById(id);    
         
        }catch(NullPointerException e){
          //ignore	
        }
         
        
        return  taxi==null;
    	
    }









}
