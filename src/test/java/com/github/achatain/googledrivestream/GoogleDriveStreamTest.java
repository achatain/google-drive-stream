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

import static java.util.List.of;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GoogleDriveStreamTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Drive drive;

    private long count;
    private String pageToken;

    @Before
    public void setUp() {
        count = -1L;
        pageToken = "page-token";
    }

    @Test
    public void streamOfThreeStorageFiles() throws Exception {
        givenStorageHasThreeFiles();
        whenFilesAreStreamed();
        thenThreeFilesAreStreamed();
    }

    private void givenStorageHasThreeFiles() throws Exception {
        FileList fileList = new FileList();
        fileList.setFiles(of(new File(), new File(), new File()));
        when(initialFileListRequest()).thenReturn(fileList);
    }

    private FileList initialFileListRequest() throws Exception {
        return drive.files().list().setFields(anyString()).setPageSize(anyInt()).execute();
    }

    private void whenFilesAreStreamed() {
        count = new GoogleDriveStream(drive).files().count();
    }

    private void thenThreeFilesAreStreamed() {
        assertEquals("Expected a stream of 3 files but got " + count,
                3L, count);
    }

    @Test
    public void streamOfTwoPagesOfThreeStorageFilesEach() throws Exception {
        givenStorageHasTwoPagesOfThreeFilesEach();
        whenFilesAreStreamed();
        thenSixFilesAreStreamed();
    }

    private void givenStorageHasTwoPagesOfThreeFilesEach() throws Exception {
        FileList fileList1 = new FileList();
        fileList1.setFiles(of(new File(), new File(), new File()));
        fileList1.setNextPageToken(pageToken);
        when(initialFileListRequest()).thenReturn(fileList1);

        FileList fileList2 = new FileList();
        fileList2.setFiles(of(new File(), new File(), new File()));
        when(subsequentFileListRequest()).thenReturn(fileList2);
    }

    private FileList subsequentFileListRequest() throws Exception {
        return drive.files().list().setFields(anyString()).setPageSize(anyInt()).setPageToken(pageToken).execute();
    }

    private void thenSixFilesAreStreamed() {
        assertEquals("Expected a stream of 6 files but got " + count,
                6L, count);
    }
}
