package com.hartwig.hmftools.common.blastn;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

import com.hartwig.hmftools.common.genome.region.Strand;

// Represents a BLASTn match with various alignment details.
public class BlastnMatch
{
    public final int QuerySeqLen;
    public final String SubjectTitle;
    public final double PercentageIdent;
    public final double QueryCoverage;
    public final int AlignmentLength;
    public final int NumMismatch;
    public final int NumGapOpenings;
    public final int SubjectAlignStart;
    public final int SubjectAlignEnd;
    public final Strand SubjectFrame;
    public final double ExpectedValue;
    public final double BitScore;
    public final String AlignedPartOfQuerySeq;
    public final String AlignedPartOfSubjectSeq;

    private int mQueryAlignStart;
    private int mQueryAlignEnd;

    public BlastnMatch(
            int querySeqLen, String subjectTitle, double percentageIdent, double queryCoverage, int alignmentLength,
            int numMismatch, int numGapOpenings, int queryAlignStart, int queryAlignEnd, int subjectAlignStart, int subjectAlignEnd,
            Strand subjectFrame, double expectedValue, double bitScore, String alignedPartOfQuerySeq, String alignedPartOfSubjectSeq)
    {
        QuerySeqLen = querySeqLen;
        SubjectTitle = subjectTitle;
        PercentageIdent = percentageIdent;
        QueryCoverage = queryCoverage;
        AlignmentLength = alignmentLength;
        NumMismatch = numMismatch;
        NumGapOpenings = numGapOpenings;
        mQueryAlignStart = queryAlignStart;
        mQueryAlignEnd = queryAlignEnd;
        SubjectAlignStart = subjectAlignStart;
        SubjectAlignEnd = subjectAlignEnd;
        SubjectFrame = subjectFrame;
        ExpectedValue = expectedValue;
        BitScore = bitScore;
        AlignedPartOfQuerySeq = alignedPartOfQuerySeq;
        AlignedPartOfSubjectSeq = alignedPartOfSubjectSeq;
    }

    public int getQuerySeqLen()
    {
        return QuerySeqLen;
    }

    public String getSubjectTitle()
    {
        return SubjectTitle;
    }

    public double getPercentageIdent()
    {
        return PercentageIdent;
    }

    public double getQueryCoverage()
    {
        return QueryCoverage;
    }

    public int getAlignmentLength()
    {
        return AlignmentLength;
    }

    public int getNumMismatch()
    {
        return NumMismatch;
    }

    public int getNumGapOpenings()
    {
        return NumGapOpenings;
    }

    public int getQueryAlignStart()
    {
        return mQueryAlignStart;
    }
    public void setQueryAlignStart(final int queryAlignStart)
    {
        mQueryAlignStart = queryAlignStart;
    }

    public int getQueryAlignEnd()
    {
        return mQueryAlignEnd;
    }
    public void setQueryAlignEnd(final int queryAlignEnd) { mQueryAlignEnd = queryAlignEnd; }

    public int getSubjectAlignStart()
    {
        return SubjectAlignStart;
    }

    public int getSubjectAlignEnd()
    {
        return SubjectAlignEnd;
    }

    public Strand getSubjectFrame()
    {
        return SubjectFrame;
    }

    public double getExpectedValue()
    {
        return ExpectedValue;
    }

    public double getBitScore()
    {
        return BitScore;
    }

    public String getAlignedPartOfQuerySeq()
    {
        return AlignedPartOfQuerySeq;
    }

    public String getAlignedPartOfSubjectSeq()
    {
        return AlignedPartOfSubjectSeq;
    }

    public static boolean isPrimaryBlastnMatch(final BlastnMatch match)
    {
        return match.getSubjectTitle().contains("Primary Assembly") ||
                match.getSubjectTitle().contains("unlocalized genomic scaffold") ||
                match.getSubjectTitle().contains("unplaced genomic scaffold");
    }

    public static double calcSumBitScore(final Collection<BlastnMatch> matches, final int minAlignmentLength)
    {
        if(matches.isEmpty())
            return 0;

        double sumBitScore = 0;

        // process all the matches and sum up the bit score, but remove the one with best match
        Optional<BlastnMatch> bestMatch = matches.stream()
                .filter(x -> isPrimaryBlastnMatch(x))
                .max(Comparator.comparing(BlastnMatch::getBitScore));

        if(bestMatch.isPresent())
        {
            sumBitScore -= bestMatch.get().getBitScore();
        }

        for(BlastnMatch m : matches)
        {
            if(m.getAlignmentLength() >= minAlignmentLength && isPrimaryBlastnMatch(m))
            {
                sumBitScore += m.getBitScore();
            }
        }

        return sumBitScore;
    }
}
