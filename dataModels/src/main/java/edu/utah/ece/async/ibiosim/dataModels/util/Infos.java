/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.utah.ece.async.ibiosim.dataModels.util;

import java.net.URI;

import com.google.common.base.Preconditions;

public class Infos {	
	public static PersonInfo forPerson(String uri) {
		return forPerson(URI.create(uri), "", null);
	}
	
	public static PersonInfo forPerson(URI uri) {
		return forPerson(uri, "", null);
	}
	
	public static PersonInfo forPerson(String uri, String name, String email) {
		return forPerson(URI.create(uri), name, email);
	}
	
	public static PersonInfo forPerson(URI uri, String name, String email) {
		return new ImmutablePersonInfo(uri, name, email);
	}
	
	private static class ImmutablePersonInfo implements PersonInfo {
		private final URI uri;
		private final String name;
		private final String email;

		public ImmutablePersonInfo(URI user, String name, String email) {
			Preconditions.checkNotNull(user, "Person URI cannot be null");
			this.uri = user;
			this.name = name;
			this.email = email;
		}

		@Override
		public URI getURI() {
		    return uri;
		}
		
		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getEmail() {
			return email;
		}

		@Override
	    public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(name);
			if (email != null) {
				sb.append(" <").append(email.toString()).append(">");
			}
		    return sb.toString();
	    }	
	}

}
