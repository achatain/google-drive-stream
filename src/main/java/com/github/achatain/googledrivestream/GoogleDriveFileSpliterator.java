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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;
import static java.lang.String.format;

public class GoogleDriveFileSpliterator implements Spliterator<File> {

    private static final int PAGE_SIZE = 1000;
    private static final String FIELDS = "files,incompleteSearch,kind,nextPageToken";
    private static final String ERROR = "Failed to fetch files for page token [%s].";

    private final Drive drive;
    private final Deque<File> files;

    private boolean firstPageFetched;
    private String nextPageToken;

    GoogleDriveFileSpliterator(Drive drive) {
        this.drive = drive;
        files = new ArrayDeque<>();
    }

    @Override
    public boolean tryAdvance(Consumer<? super File> fileConsumer) {
        try {
            if (!firstPageFetched)
                fetchFirstPage();

            if (files.isEmpty())
                if (hasNextPage()) {
                    fetchNextPage();
                    return tryAdvance(fileConsumer);
                } else
                    return false;
            else {
                fileConsumer.accept(files.pop());
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(format(ERROR, nextPageToken), e);
        }
    }

    private void fetchFirstPage() throws IOException {
        FileList firstPage = drive.files().list().setFields(FIELDS).setPageSize(PAGE_SIZE).execute();
        files.addAll(firstPage.getFiles());
        nextPageToken = firstPage.getNextPageToken();
        firstPageFetched = true;
    }

    private boolean hasNextPage() {
        return nonNull(nextPageToken);
    }

    private void fetchNextPage() throws IOException {
        FileList nextPage = drive.files().list().setFields(FIELDS).setPageSize(PAGE_SIZE).setPageToken(nextPageToken).execute();
        files.addAll(nextPage.getFiles());
        nextPageToken = nextPage.getNextPageToken();
    }

    @Override
    public Spliterator<File> trySplit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return 0;
    }
}
