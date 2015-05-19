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

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;

/**
 * <p>This class provides methods to check Taxi objects against arbitrary requirements.</p>
 * 
 * @author Joshua Wilson
 * @see Taxi
 * @see TaxiRepository
 * @see javax.validation.Validator
 */
public class TaxiValidator {
    @Inject
    private Validator validator;

    @Inject
    private TaxiRepository crud;

    /**
     * <p>Validates the given Taxi object and throws validation exceptions based on the type of error. If the error is standard
     * bean validation errors then it will throw a ConstraintValidationException with the set of the constraints violated.<p/>
     *
     *
     * <p>If the error is caused because an existing taxi with the same reg is registered it throws a regular validation
     * exception so that it can be interpreted separately.</p>
     *
     * 
     * @param taxi The Taxi object to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If taxi with the same reg already exists
     */
    void validateTaxi(Taxi taxi) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Taxi>> violations = validator.validate(taxi);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the reg
        if (regAlreadyExists(taxi.getReg(), taxi.getId())) {
            throw new ValidationException("Unique reg Violation");
        }
    }

    /**
     * <p>Checks if a taxi with the same reg is already registered. This is the only way to easily capture the
     * "@UniqueConstraint(columnNames = "reg")" constraint from the Taxi class.</p>
     * 
     * <p>Since Update will being using a reg that is already in the database we need to make sure that it is the reg
     * from the record being updated.</p>
     * 
     * @param reg The reg to check is unique
     * @param id The user id to check the reg against if it was found
     * @return boolean which represents whether the reg was found, and if so if it belongs to the user with id
     */
    boolean regAlreadyExists(String reg, Long id) {
        Taxi taxi = null;
        Taxi taxiWithID = null;
        try {
            taxi = crud.findByReg(reg);
        } catch (NoResultException e) {
            // ignore
        }

        if (taxi != null && id != null) {
            try {
                taxiWithID = crud.findById(id);
                if (taxiWithID != null && taxiWithID.getReg().equals(reg)) {
                    taxi = null;
                }
            } catch (NoResultException e) {
                // ignore
            }
        }
        return taxi != null;
    }
}
