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
package org.jboss.quickstarts.wfk.customer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import java.util.List;
import java.util.logging.Logger;

/**
 * <p>This is a Repository class and connects the Service/Control layer (see {@link CustomerService} with the
 * Domain/Entity Object (see {@link Customer}).<p/>
 *
 * <p>There are no access modifiers on the methods making them 'package' scope.  They should only be accessed by a
 * Service/Control object.<p/>
 * 
 * @author Joshua Wilson
 * @see Customer
 * @see javax.persistence.EntityManager
 */
public class CustomerRepository {

    @Inject
    private @Named("logger") Logger log;

    @Inject
    private EntityManager em;
    
    /**
     * <p>Returns a List of all persisted {@link Customer} objects, sorted alphabetically by last name.</p>
     * 
     * @return List of customer objects
     */
    List<Customer> findAllOrderedByName() {
        TypedQuery<Customer> query = em.createNamedQuery(Customer.FIND_ALL, Customer.class); 
        return query.getResultList();
    }

    /**
     * <p>Returns a single customer object, specified by a Long id.<p/>
     *
     * @param id The id field of the customer to be returned
     * @return The customer with the specified id
     */
    public Customer findById(Long id) {
        return em.find(Customer.class, id);
    }

    /**
     * <p>Returns a single customer object, specified by a String email.</p>
     *
     * <p>If there is more than one customer with the specified email, only the first encountered will be returned.<p/>
     *
     * @param email The email field of the customer to be returned
     * @return The first customer with the specified email
     */
    Customer findByEmail(String email) {
        TypedQuery<Customer> query = em.createNamedQuery(Customer.FIND_BY_EMAIL, Customer.class).setParameter("email", email); 
        return query.getSingleResult();
    }

    /**
     * <p>Returns a single customer object, specified by a String firstName.<p/>
     *
     * <p>If there is more then one, only the first will be returned.<p/>
     *
     * @param firstName The firstName field of the customer to be returned
     * @return The first customer with the specified firstName
     */
    Customer findByName(String Name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Customer> criteria = cb.createQuery(Customer.class);
        Root<Customer> customer = criteria.from(Customer.class);
        // Swap criteria statements if you would like to try out type-safe criteria queries, a new feature in JPA 2.0.
        // criteria.select(customer).where(cb.equal(customer.get(customer_.firstName), firstName));
        criteria.select(customer).where(cb.equal(customer.get("customerName"), Name));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * <p>Returns a single customer object, specified by a String lastName.<p/>
     *
     * <p>If there is more then one, only the first will be returned.<p/>
     *
     * @param lastName The lastName field of the customer to be returned
     * @return The first customer with the specified lastName
     */
 

    /**
     * <p>Persists the provided customer object to the application database using the EntityManager.</p>
     *
     * <p>{@link javax.persistence.EntityManager#persist(Object) persist(Object)} takes an entity instance, adds it to the
     * context and makes that instance managed (ie future updates to the entity will be tracked)</p>
     *
     * <p>persist(Object) will set the @GeneratedValue @Id for an object.</p>
     *
     * @param customer The customer object to be persisted
     * @return The customer object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Customer create(Customer customer) throws ConstraintViolationException, ValidationException, Exception {
        log.info("customerRepository.create() - Creating " + customer.getName());
        
        // Write the customer to the database.
        em.persist(customer);
        
        return customer;
    }

    /**
     * <p>Updates an existing customer object in the application database with the provided customer object.</p>
     * 
     * <p>{@link javax.persistence.EntityManager#merge(Object) merge(Object)} creates a new instance of your entity,
     * copies the state from the supplied entity, and makes the new copy managed. The instance you pass in will not be
     * managed (any changes you make will not be part of the transaction - unless you call merge again).</p>
     * 
     * <p>merge(Object) however must have an object with the @Id already generated.</p>
     * 
     * @param customer The customer object to be merged with an existing customer
     * @return The customer that has been merged
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Customer update(Customer customer) throws ConstraintViolationException, ValidationException, Exception {
        log.info("customerRepository.update() - Updating " + customer.getName());
        
        // Either update the customer or add it if it can't be found.
        em.merge(customer);
        
        return customer;
    }

    /**
     * <p>Deletes the provided customer object from the application database if found there</p>
     *
     * @param customer The customer object to be removed from the application database
     * @return The customer object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    Customer delete(Customer customer) throws Exception {
        log.info("customerRepository.delete() - Deleting " + customer.getName());
        
        if (customer.getId() != null) {
            /*
             * The Hibernate session (aka EntityManager's persistent context) is closed and invalidated after the commit(), 
             * because it is bound to a transaction. The object goes into a detached status. If you open a new persistent 
             * context, the object isn't known as in a persistent state in this new context, so you have to merge it. 
             * 
             * Merge sees that the object has a primary key (id), so it knows it is not new and must hit the database 
             * to reattach it. 
             * 
             * Note, there is NO remove method which would just take a primary key (id) and a entity class as argument. 
             * You first need an object in a persistent state to be able to delete it.
             * 
             * Therefore we merge first and then we can remove it.
             */
            em.remove(em.merge(customer));
            
        } else {
            log.info("customerRepository.delete() - No ID was found so can't Delete.");
        }
        
        return customer;
    }

}
