package com.github.timurstrekalov.saga.core;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

class FileStats {

    private static final Logger logger = LoggerFactory.getLogger(FileStats.class);

    private final String fullName;
    private final String relativeName;
    private final String fileName;
    private final String parentName;
    private final String id;
    private final List<LineCoverageRecord> lineCoverageRecords;

    FileStats(final String fullName, final List<LineCoverageRecord> lineCoverageRecords) {
        this.fullName = fullName;
        this.relativeName = getRelativeName(fullName);

        final File file = new File(relativeName);
        fileName = file.getName();
        parentName = file.getParent();

        this.id = generateId();
        this.lineCoverageRecords = lineCoverageRecords;
    }

    private String getRelativeName(String fullName) {
        String relativeName;

        try {
            relativeName = ResourceUtils.getRelativePath(fullName,
                    new File(System.getProperty("user.dir")).toURI().toString(), File.separator);
        } catch (final Exception e) {
            logger.debug(e.getMessage(), e);
            relativeName = fullName;
        }

        return relativeName;
    }

    private String generateId() {
        return DigestUtils.md5Hex(fullName);
    }

    public List<LineCoverageRecord> getLineCoverageRecords() {
        return lineCoverageRecords;
    }

    public Collection<LineCoverageRecord> getExecutableLineCoverageRecords() {
        return Collections2.filter(lineCoverageRecords, new Predicate<LineCoverageRecord>() {
            @Override
            public boolean apply(final LineCoverageRecord record) {
                return record.isExecutable();
            }
        });
    }

    public int getStatements() {
        return Collections2.filter(lineCoverageRecords, new Predicate<LineCoverageRecord>() {
            @Override
            public boolean apply(final LineCoverageRecord input) {
                return input.isExecutable();
            }
        }).size();
    }

    public int getExecuted() {
        return Util.sum(Collections2.transform(lineCoverageRecords, new Function<LineCoverageRecord, Integer>() {
            @Override
            public Integer apply(final LineCoverageRecord input) {
                return input.getTimesExecuted() > 0 ? 1 : 0;
            }
        }));
    }

    public int getCoverage() {
        return Util.toCoverage(getStatements(), getExecuted());
    }

    public boolean getHasStatements() {
        return getStatements() > 0;
    }

    public String getBarColor() {
        return Util.getColor(getCoverage());
    }

    static FileStats merge(final FileStats s1, final FileStats s2) {
        final List<LineCoverageRecord> r1 = s1.getLineCoverageRecords();
        final List<LineCoverageRecord> r2 = s2.getLineCoverageRecords();

        Validate.isTrue(s1.fullName.equals(s2.fullName));
        Validate.isTrue(r1.size() == r2.size());

        final List<LineCoverageRecord> mergedRecords = Lists.newLinkedList();

        for (int i = 0; i < r1.size(); i++) {
            final LineCoverageRecord l1 = r1.get(i);
            final LineCoverageRecord l2 = r2.get(i);

            mergedRecords.add(LineCoverageRecord.merge(l1, l2));
        }

        return new FileStats(s1.fullName, mergedRecords);
    }

    public String getFileName() {
        return normalizeFileSeparators(fileName);
    }

    public String getFullName() {
        return normalizeFileSeparators(fullName);
    }

    public String getRelativeName() {
        return normalizeFileSeparators(relativeName);
    }

    public String getParentName() {
        return normalizeFileSeparators(parentName);
    }

    public String getFilePath() {
        try {
            return new File(new URI(getFullName())).getAbsolutePath();
        } catch (final URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getId() {
        return id;
    }

    private String normalizeFileSeparators(final String path) {
        if (path == null) {
            return null;
        }

        return path.replaceAll("\\\\", "/");
    }

}
