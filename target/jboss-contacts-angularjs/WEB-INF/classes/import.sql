--
-- JBoss, Home of Professional Open Source
-- Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
-- contributors by the @authors tag. See the copyright.txt in the
-- distribution for a full listing of individual contributors.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- http://www.apache.org/licenses/LICENSE-2.0
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

-- You can use this file to load seed data into the database using SQL statements
-- Since the database doesn't know to increase the Sequence to match what is manually loaded here it starts at 1 and tries
--  to enter a record with the same PK and create an error.  If we use a high we don't interfere with the sequencing (at least until later).
-- NOTE: this file should be removed for production systems. 
insert into Customer (id, customerName, email, phone_number) values (10001, 'John',  'john.smith@mailinator.com', '05263987417')
insert into Customer (id, customerName, email, phone_number) values (10002, 'Davey', 'davey.jones@locker.com', '02935687415')
insert into Taxi (id, num_seats, reg) values (100001,7,'p799sng')
insert into Taxi (id, num_seats, reg) values (100002,5,'p799snh')
insert into Booking (id,customerId,taxiId,booking_date) values (1000001,10001,100001,'2016-08-07')