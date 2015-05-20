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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.quickstarts.wfk.customer.Customer;
import org.jboss.quickstarts.wfk.taxi.Taxi;

/**
 * <p>This is a the Domain object. The Booking class represents how booking resources are represented in the application
 * database.</p>
 *
 * <p>The class also specifies how a bookings are retrieved from the database (with @NamedQueries), and acceptable values
 * for Booking fields (with @NotNull, @Pattern etc...)<p/>
 * 
 * @author Joshua Wilson
 */
/*
 * The @NamedQueries included here are for searching against the table that reflects this object.  This is the most efficient
 * form of query in JPA though is it more error prone due to the syntax being in a String.  This makes it harder to debug.
 */
@Entity
@NamedQueries({
    @NamedQuery(name = Booking.FIND_ALL, query = "SELECT c FROM Booking c ORDER BY c.id ASC"),
    @NamedQuery(name = Booking.FIND_BY_CUSTOMER, query = "SELECT c FROM Booking c WHERE c.customer.id=:customerId"),
    @NamedQuery(name = Booking.FIND_BY_TAXI, query="SELECT c FROM Booking c WHERE c.taxi.id=:taxiId")
})
@XmlRootElement
@Table(name = "Booking", uniqueConstraints = @UniqueConstraint(columnNames = "id"))


public class Booking implements Serializable {
    /** Default value included to remove warning. Remove or modify at will. **/
    private static final long serialVersionUID = 1L;
    
    public static final String FIND_ALL = "Booking.findAll";
    public static final String FIND_BY_TAXI = "Booking.findByTaxi";
    public static final String FIND_BY_CUSTOMER = "Booking.findByCustomer";
    /*
     * The  error messages match the ones in the UI so that the user isn't confused by two similar error messages for
     * the same error after hitting submit. This is if the form submits while having validation errors. The only
     * difference is that there are no periods(.) at the end of these message sentences, this gives us a way to verify
     * where the message came from.
     * 
     * Each variable name exactly matches the ones used on the HTML form name attribute so that when an error for that
     * variable occurs it can be sent to the correct input field on the form.  
     */
    
    //ID generated automatically
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

 
    //booking date
    @NotNull
    @Future(message = "Booking dates can not be in the past. Please choose one from the future")
    @Column(name = "booking_date")
    @Temporal(TemporalType.DATE)
    private Date booking_date;

    @ManyToOne
    //@NotNull
    @JoinColumn(name="customerId")
    private Customer customer;
   
    @ManyToOne
  //  @NotNull
    @JoinColumn(name="taxiId")
    private Taxi taxi;
    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
     
    
    public Customer getCustomer()
    {
    	return customer;
    }

    public void setCustomer(Customer customer)
    {
    	this.customer=customer;
    }
    
    public long getCustomerId()
    {
       return this.customer.getId();	
    }
    
    public void setCustomerId(long cusID)
    {
    	this.customer.setId(cusID);
    }
    
    
    public Taxi getTaxi()
    {
    	return taxi;
    }
    
    public void setTaxi(Taxi tax)
    {
    	this.taxi=tax;
    }
    
    public long getTaxiId()
    {
    	return this.taxi.getId();
    }
    
    public void setTaxiId(long taxiId)
    {
    	this.taxi.setId(taxiId);
    }
    
    public Date getBookingDate() {
        return booking_date;
    }

    public void setBookingDate(Date date) {
        this.booking_date = date;
    }

   
}
