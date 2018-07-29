/**
 * MIT License
 *
 * Copyright (c) 2018 Antoine R. "achatain" (achatain [at] outlook [dot] com)
 *
 * https://github.com/achatain/google-drive-stream
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.achatain.googledrivestream;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.List.of;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoogleDriveFileSpliteratorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Drive drive;

    private GoogleDriveFileSpliterator spliterator;
    private ConsumerTestHelper consumer;
    private FileList fileList;
    private File file1;
    private File file2;

    @Before
    public void setUp() throws Exception {
        consumer = new ConsumerTestHelper();
        fileList = new FileList();
        file1 = new File();
        file2 = new File();
        spliterator = new GoogleDriveFileSpliterator(drive);

        when(drive.files().list().execute()).thenReturn(fileList);
    }

    @Test
    public void doNotConsumeFileWhenNoFileExists() {
        givenStorageHasNoFile();
        thenNoFileCanBeConsumed();
    }

    private void givenStorageHasNoFile() {
        fileList.setFiles(emptyList());
    }

    private void thenNoFileCanBeConsumed() {
        assertFalse("No file should have been consumed",
                spliterator.tryAdvance(consumer));
    }

    @Test
    public void consumeTheFileWhenStorageHasOneFile() {
        givenStorageHasOneFile();
        whenNextFileIsConsumed();
        thenFileConsumedIs(file1);
    }

    private void givenStorageHasOneFile() {
        fileList.setFiles(singletonList(file1));
    }

    private void whenNextFileIsConsumed() {
        assertTrue("A file should have been consumed",
                spliterator.tryAdvance(consumer));
    }

    private void thenFileConsumedIs(File file) {
        assertTrue("An unexpected file was consumed",
                consumer.fileIs(file));
    }

    @Test
    public void consumeFirstThenSecondFilesWhenStorageHasTwoFiles() {
        givenStorageHasTwoFiles();
        whenNextFileIsConsumed();
        thenFileConsumedIs(file1);
        whenNextFileIsConsumed();
        thenFileConsumedIs(file2);
    }

    private void givenStorageHasTwoFiles() {
        fileList.setFiles(of(file1, file2));
    }

    private static class ConsumerTestHelper implements Consumer<File> {

        private File file;

        @Override
        public void accept(File file) {
            this.file = file;
        }

        boolean fileIs(File file) {
            return this.file == file;
        }
    }
}
