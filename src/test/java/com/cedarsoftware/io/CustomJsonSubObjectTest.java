package com.cedarsoftware.io;

import java.io.IOException;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.Map;

import com.cedarsoftware.util.DeepEquals;
import org.junit.jupiter.api.Test;

/**
 * @author John DeRegnaucourt (jdereg@gmail.com)
 *         <br>
 *         Copyright (c) Cedar Software LLC
 *         <br><br>
 *         Licensed under the Apache License, Version 2.0 (the "License")
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *         <br><br>
 *         <a href="http://www.apache.org/licenses/LICENSE-2.0">License</a>
 *         <br><br>
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */
class CustomJsonSubObjectTest
{
	static class Person {
		private String firstName;
		private String lastName;
		private String phoneNumber;
		private OffsetDateTime dob;
		private TestObjectKid kid;
	}

	class PersonFactory implements JsonReader.ClassFactory {
		public Object newInstance(Class<?> c, JsonObject jObj, Resolver resolver) {
			Person person = new Person();
			Map<String, Object> map = (Map) jObj;
			person.firstName = (String) map.get("first");
			person.lastName = (String) map.get("last");
			person.phoneNumber = (String) map.get("phone");
			person.dob = OffsetDateTime.parse((String) map.get("dob"));
			person.kid = (TestObjectKid)resolver.createInstance((JsonObject)map.get("kid"));
			resolver.push((JsonObject)map.get("kid"));
			return person;
		}
	}

	class PersonWriter implements JsonWriter.JsonClassWriter {
		public void write(Object o, boolean showType, Writer output, WriterContext context) throws IOException {
			Person p = (Person) o;
			output.write("\"first\":\"");
			output.write(p.firstName);
			output.write("\",\"last\":\"");
			output.write(p.lastName);
			output.write("\",\"phone\":\"");
			output.write(p.phoneNumber);
			output.write("\"");
			output.write(",\"dob\":\"");
			output.write(p.dob.toString());
			output.write("\",\"kid\":");
			context.writeObject(p.kid, false, false);
		}
	}

	Person createPersonJohn() {
		Person john = new Person();
		john.firstName = "John";
		john.lastName = "Smith";
		john.phoneNumber = "213-555-1212";
		john.dob = OffsetDateTime.parse("1976-07-04T16:20:00-08:00");
		john.kid = new TestObjectKid("Lisa", "lisa@gmail.com");
		return john;
	}

	@Test
	void testCustomReaderSimple()
	{
		ReadOptionsBuilder readOptions = new ReadOptionsBuilder()
				.addClassFactory(Person.class, new PersonFactory())
				.aliasTypeName(Person.class, "Person")
				.withExtendedAliases();

		WriteOptionsBuilder writeOptions = new WriteOptionsBuilder()
				.addCustomWrittenClass(Person.class, new PersonWriter())
				.aliasTypeName(Person.class, "Person")
				.withExtendedAliases();

		Person p1 = createPersonJohn();
		String json = JsonIo.toJson(p1, writeOptions.build());
		System.out.println(json);
		Person p2 = JsonIo.toObjects(json, readOptions.build(), Person.class);

		assert DeepEquals.deepEquals(p1, p2);
	}
}
