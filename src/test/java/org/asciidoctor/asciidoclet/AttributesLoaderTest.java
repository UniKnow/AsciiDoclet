/*
 * Copyright 2013-2018 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.asciidoclet;

import org.asciidoctor.Asciidoctor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AttributesLoaderTest {
    static final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private final StubReporter reporter = new StubReporter();

    @Rule
    public final TemporaryFolder tmpDir = new TemporaryFolder();

    @Test
    public void testNoAttributes() {
        DocletOptions options = new DocletOptions( reporter );
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, reporter);

        Map<String, Object> attrs = loader.load();

        assertTrue(attrs.isEmpty());
        reporter.assertNoMoreInteractions();
    }

    @Test
    public void testOnlyCommandLineAttributes() {
        DocletOptions options = new DocletOptions( reporter );
        options.collect( AsciidocletOptions.ATTRIBUTE, List.of( "foo=bar, foo2=foo-two, not!, override=override@" ) );
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, reporter);

        Map<String, Object> attrs = loader.load();

        assertEquals( attrs.get( "foo" ), "bar" );
        assertEquals( attrs.get( "foo2" ), "foo-two" );
        assertEquals( attrs.get( "override" ), "override@" );
        assertFalse( attrs.containsKey( "not" ) );
        assertTrue( attrs.containsKey( "not!" ) );
        reporter.assertNoMoreInteractions();
    }

    @Test
    public void testOnlyCommandLineAttributesMulti() {
        DocletOptions options = new DocletOptions( reporter );
        options.collect( AsciidocletOptions.ATTRIBUTE, List.of(
                "foo=bar", "foo2=foo two", "not!", "override=override@" ) );
        AttributesLoader loader = new AttributesLoader(asciidoctor, options, reporter);

        Map<String, Object> attrs = loader.load();

        assertEquals( attrs.get( "foo" ), "bar" );
        assertEquals( attrs.get( "foo2" ), "foo two" );
        assertEquals( attrs.get( "override" ), "override@" );
        assertFalse( attrs.containsKey( "not" ) );
        assertTrue( attrs.containsKey( "not!" ) );
        reporter.assertNoMoreInteractions();
    }

    private File createTempFile(String name, String content) throws IOException
    {
        File file = tmpDir.newFile(name);
        writeFile( content, file );
        return file;
    }

    private File createTempFile(String dir, String name, String content) throws IOException {
        File directory = tmpDir.newFolder(dir);
        File file = new File(directory, name);
        writeFile( content, file );
        return file;
    }

    private void writeFile( String content, File file ) throws IOException
    {
        byte[] bytes = content.getBytes( StandardCharsets.UTF_8 );
        Files.write( file.toPath(), bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING );
    }

    static final String ATTRS =
            ":foo: BAR\n" +
            ":foo2: {foo}-TWO\n" +
            ":not: FOO\n" +
            ":override: OVERRIDE\n";
}
