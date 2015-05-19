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
(function() {
    'use strict';
    angular
        .module('app')
        .controller('AppHotelController', AppHotelController);

    AppHotelController.$inject = ['$scope', '$filter', 'Hotel', 'messageBag'];

    function AppHotelController($scope, $filter, Hotel, messageBag) {
        //Assign Contact service to $scope variable
        $scope.hotels = Hotel;
        //Assign Messages service to $scope variable
        $scope.messages = messageBag;

        //Divide hotel list into several sub lists according to the first character of their firstName property
        var getHeadings = function(hotels) {
            var headings = {};
            for(var i = 0; i<hotels.length; i++) {
                //Get the first letter of a contact's firstName
                var startsWithLetter = hotels[i].hotelName.charAt(0).toUpperCase();
                //If we have encountered that first letter before then add the contact to that list, else create it
                if(headings.hasOwnProperty(startsWithLetter)) {
                    headings[startsWithLetter].push(hotels[i]);
                } else {
                    headings[startsWithLetter] = [hotels[i]];
                }
            }
            return headings;
        };

        //Upon initial loading of the controller, populate a list of Contacts and their letter headings
        $scope.hotels.data = $scope.hotels.query(
            //Successful query
            function(data) {
                $scope.hotels.data = data;
                $scope.hotelsList = getHeadings($scope.hotels.data);
                //Keep the contacts list headings in sync with the underlying contacts
                $scope.$watchCollection('hotels.data', function(newHotels, oldHotels) {
                    $scope.hotelsList = getHeadings(newHotels);
                });
            },
            //Error
            function(result) {
                for(var error in result.data){
                    $scope.messages.push('danger', result.data[error]);
                }
            }
        );

        //Boolean flag representing whether the details of the contacts are expanded inline
        $scope.details = false;

        //Default search string
        $scope.search = "";

        //Continuously filter the content of the contacts list according to the contents of $scope.search
        $scope.$watch('search', function(newValue, oldValue) {
            $scope.hotelsList = getHeadings($filter('filter')($scope.hotels.data, $scope.search));
        });
    }
})();