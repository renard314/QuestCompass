/*
 * Copyright (C) 2011 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.renard.radar;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

public class LocationListDaoGenerator {

    public static void main(String[] args) throws Exception {
        System.out.println("Creating ORM files");
        Schema schema = new Schema(3, "gen.radar");

        addLocation(schema);

        new DaoGenerator().generateAll(schema, "../../app/src");
    }

    private static void addLocation(Schema schema) {
        Entity note = schema.addEntity("Location");
        note.addIdProperty();

        note.addBooleanProperty("active").notNull();
        note.addDoubleProperty("lat").notNull();
        note.addDoubleProperty("lng").notNull();
        note.addStringProperty("description").notNull();
        note.addDateProperty("created").notNull();
    }



}
