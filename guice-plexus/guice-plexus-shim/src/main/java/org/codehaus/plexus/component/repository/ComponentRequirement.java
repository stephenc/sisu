/**
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.codehaus.plexus.component.repository;

import org.sonatype.guice.plexus.config.Hints;

public final class ComponentRequirement
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private String role;

    private String hint = Hints.DEFAULT_HINT;

    private String name;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void setRole( final String role )
    {
        this.role = role;
    }

    public void setRoleHint( final String hint )
    {
        this.hint = Hints.canonicalHint( hint );
    }

    public void setFieldName( final String name )
    {
        this.name = name;
    }

    public String getRole()
    {
        return role;
    }

    public String getRoleHint()
    {
        return hint;
    }

    public String getFieldName()
    {
        return name;
    }
}
