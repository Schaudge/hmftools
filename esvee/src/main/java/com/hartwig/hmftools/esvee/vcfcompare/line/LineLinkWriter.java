package com.hartwig.hmftools.esvee.vcfcompare.line;

import static com.hartwig.hmftools.common.utils.file.FileDelimiters.TSV_DELIM;
import static com.hartwig.hmftools.esvee.assembly.AssemblyConfig.SV_LOGGER;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hartwig.hmftools.common.utils.file.FileWriterUtils;
import com.hartwig.hmftools.esvee.vcfcompare.Breakend;
import com.hartwig.hmftools.esvee.vcfcompare.CompareConfig;

import org.jetbrains.annotations.Nullable;

public class LineLinkWriter
{
    public final CompareConfig mConfig;

    public LineLinkWriter(final CompareConfig config)
    {
        mConfig = config;
    }

    private static final String OLD_PREFIX = "Old";
    private static final String NEW_PREFIX = "New";

    private static final List<String> FIXED_HEADER_FIELDS = List.of(
            "UnifiedPolyACoords",
            "UnifiedPolyACoordsCount",
            "PolyAMatchType",
            "HasAnyPass"
    );

    private static final List<String> HEADER_SUFFIXES = List.of(
            "VcfType",
            "LinkType",
            "PolyAId",
            "PolyARemoteId",
            "OtherId",
            "OtherRemoteId",
            "PolyACoords",
            "PolyARemoteCoords",
            "OtherCoords",
            "OtherRemoteCoords",
            "PolyAInsertSeq",
            "OtherInsertSeq",
            "PolyASvType",
            "OtherSvType",
            "PolyAFilter",
            "OtherFilter",
            "PolyAQual",
            "OtherQual",
            "PolyAFrags",
            "OtherFrags"
    );

    private static String header()
    {
        List<String> headerStrings = new ArrayList<>();

        headerStrings.addAll(FIXED_HEADER_FIELDS);

        // Alternating
        for(String suffix : HEADER_SUFFIXES)
        {
            headerStrings.add(OLD_PREFIX + suffix);
            headerStrings.add(NEW_PREFIX + suffix);
        }

        return String.join(TSV_DELIM, headerStrings);
    }

    private BufferedWriter initialiseWriter()
    {
        try
        {
            String fileName = ""; // mConfig.formFilename("line");

            SV_LOGGER.debug("writing LINE comparison file: {}", fileName);

            BufferedWriter writer = FileWriterUtils.createBufferedWriter(fileName, false);
            writer.write(header());
            writer.newLine();

            return writer;
        }
        catch(IOException e)
        {
            SV_LOGGER.error("Failed to initialise output file: {}", e.toString());
            System.exit(1);
            return null;
        }
    }

    private class BreakendRowValues
    {
        String VcfType = "";
        String LinkType = "";

        String PolyAId = "";
        String OtherId = "";
        String PolyARemoteId = "";
        String OtherRemoteId = "";

        String PolyACoords = "";
        String OtherCoords = "";
        String PolyARemoteCoords = "";
        String OtherRemoteCoords = "";

        String PolyAInsertSeq = "";
        String OtherInsertSeq = "";

        String PolyASvType = "";
        String OtherSvType = "";

        String PolyAFilter = "";
        String OtherFilter = "";

        String PolyAQual = "";
        String OtherQual = "";

        String PolyAFrags = "";
        String OtherFrags = "";

        public BreakendRowValues(@Nullable Breakend breakend)
        {
            if(breakend == null)
                return;

            boolean hasLink = breakend.hasLineLink();
            LineLink lineLink;
            Breakend polyASite;
            Breakend otherSite;

            if(hasLink)
            {
                LinkType = breakend.lineData().LinkedLineBreakends.mType.toString();

                lineLink = breakend.lineData().LinkedLineBreakends;
                polyASite = lineLink.mPolyASite;
                otherSite = lineLink.mOtherSite;
            }
            else
            {
                LinkType = LineLinkType.NO_LINK.toString();

                lineLink = null;
                polyASite = breakend;
                otherSite = null;
            }

            setPolyASiteValues(polyASite, lineLink);

            if(otherSite != null)
            {
                setOtherSiteValues(otherSite, lineLink);
            }
        }

        private void setPolyASiteValues(final Breakend polyASite, @Nullable LineLink lineLink)
        {
            VcfType = null; // polyASite.SourceVcfType.toString();

            PolyAId = polyASite.sv().id();
            PolyACoords = polyASite.coordStr();
            PolyAInsertSeq = polyASite.InsertSequence;

            /*
            PolyASvType = polyASite.SvType;
            PolyAFilter = polyASite.filtersStr();
            PolyAQual = polyASite.qualStr();
            PolyAFrags = ""; // polyASite.fragsStr(mConfig.SampleId);

            if(!polyASite.isSingle() && (lineLink == null || lineLink.polyAHasRemote()))
            {
                PolyARemoteCoords = polyASite.otherCoordStr();
                PolyARemoteId = polyASite.mateId();
            }
            */
        }

        private void setOtherSiteValues(final Breakend otherSite, @Nullable LineLink lineLink)
        {
            /*
            OtherId = otherSite.Id;
            OtherCoords = otherSite.coordStr();
            OtherInsertSeq = otherSite.InsertSequence;
            OtherSvType = otherSite.SvType;
            OtherFilter = otherSite.filtersStr();
            OtherQual = otherSite.qualStr();
            OtherFrags = ""; // otherSite.fragsStr(mConfig.SampleId);

            if(!otherSite.isSingle() && (lineLink == null || lineLink.otherHasRemote()))
            {
                OtherRemoteCoords = otherSite.otherCoordStr();
                OtherRemoteId = otherSite.mateId();
            }
            */
        }

        private void setInferredOtherSiteValues(final Breakend otherBreakend)
        {
            LineLink inferredLink = otherBreakend.lineData().InferredLinkedLineBreakends;

            SV_LOGGER.trace("Used inferred link in breakend[{}] to assign otherSite[{}]", otherBreakend, inferredLink.mOtherSite);
            setOtherSiteValues(inferredLink.mOtherSite, inferredLink);

            LinkType = inferredLink.mType.toString();
        }

        public List<String> getOutputStrings()
        {
            return List.of(
                    VcfType,
                    LinkType,
                    PolyAId,
                    PolyARemoteId,
                    OtherId,
                    OtherRemoteId,
                    PolyACoords,
                    PolyARemoteCoords,
                    OtherCoords,
                    OtherRemoteCoords,
                    PolyAInsertSeq,
                    OtherInsertSeq,
                    PolyASvType,
                    OtherSvType,
                    PolyAFilter,
                    OtherFilter,
                    PolyAQual,
                    OtherQual,
                    PolyAFrags,
                    OtherFrags
            );
        }
    }

    private class RowValues
    {
        BreakendRowValues mBreakend1RowValues;
        BreakendRowValues mBreakend2RowValues;

        public RowValues(@Nullable Breakend breakend1, @Nullable Breakend breakend2)
        {
            if(breakend1 == null && breakend2 == null)
                throw new IllegalStateException("`breakend1` and `breakend2` cannot both be null");

            mBreakend1RowValues = new BreakendRowValues(breakend1);
            mBreakend2RowValues = new BreakendRowValues(breakend2);

            if(breakend2 == null && breakend1.hasInferredLineLink())
            {
                mBreakend2RowValues.setInferredOtherSiteValues(breakend1);
            }

            if(breakend1 == null && breakend2.hasInferredLineLink())
            {
                mBreakend1RowValues.setInferredOtherSiteValues(breakend2);
            }
        }

        public List<String> getAlternatingStrings(){

            List<String> breakend1Strings = mBreakend1RowValues.getOutputStrings();
            List<String> breakend2Strings = mBreakend2RowValues.getOutputStrings();

            List<String> rowStrings = new ArrayList<>();
            for(int i = 0; i < breakend1Strings.size(); i++)
            {
                rowStrings.add(breakend1Strings.get(i));
                rowStrings.add(breakend2Strings.get(i));
            }

            return rowStrings;
        }
    }

    private boolean isLineInsertSiteOfInterest(@Nullable Breakend breakend)
    {
        return breakend != null &&
                (mConfig.IncludeNonPass || breakend.isPass()) &&
                breakend.hasPolyATail();
    }

    private static String getUnifiedPolyACoords(Breakend oldBreakend, Breakend newBreakend)
    {
        if(oldBreakend != null)
        {
            if(!oldBreakend.hasLineLink())
                return oldBreakend.coordStr();
            else
                return oldBreakend.lineData().InferredLinkedLineBreakends.mPolyASite.coordStr();
        }
        else
        {
            if(!newBreakend.hasLineLink())
                return newBreakend.coordStr();
            else
                return newBreakend.lineData().LinkedLineBreakends.mPolyASite.coordStr();
        }
    }

    /*
    private HashMap<String, Integer> countUnifiedPolyACoords(List<BreakendMatch> breakendMatches)
    {
        HashMap<String, Integer> countMap = new HashMap<>();

        for(BreakendMatch match : breakendMatches)
        {
            Breakend oldBreakend = match.OldBreakend;
            Breakend newBreakend = match.NewBreakend;

            if(!isLineInsertSiteOfInterest(oldBreakend) && !isLineInsertSiteOfInterest(newBreakend))
                continue;

            String coords = getUnifiedPolyACoords(oldBreakend, newBreakend);

            countMap.merge(coords, 1, Integer::sum);
        }

        return countMap;
    }

     */

    private static boolean variantHasAnyPass(@Nullable final Breakend breakend)
    {
        boolean anyPass;

        if(breakend == null)
        {
            anyPass = false;
        }
        else if(!breakend.hasLineLink())
        {
            anyPass = breakend.isPass();
        }
        else
        {
            anyPass = breakend.lineData().LinkedLineBreakends.mPolyASite.isPass();
            anyPass |= breakend.lineData().LinkedLineBreakends.mOtherSite.isPass();
        }

        return anyPass;
    }

    public void writeBreakends()
    {
        try
        {
            BufferedWriter writer = initialiseWriter();

            /*
            List<BreakendMatch> breakendMatches = mBreakendMatcher.getBreakendMatches();

            HashMap<String, Integer> unifiedPolyACoordsCountMap = countUnifiedPolyACoords(breakendMatches);

            for(BreakendMatch match : breakendMatches)
            {
                Breakend oldBreakend = match.OldBreakend;
                Breakend newBreakend = match.NewBreakend;

                List<String> rowStrings = new ArrayList<>();

                if(!isLineInsertSiteOfInterest(oldBreakend) && !isLineInsertSiteOfInterest(newBreakend))
                    continue;

                String unifiedPolyACoords = getUnifiedPolyACoords(oldBreakend, newBreakend);
                rowStrings.add(unifiedPolyACoords);

                Integer unifiedPolyACoordsCount = unifiedPolyACoordsCountMap.get(unifiedPolyACoords);
                rowStrings.add(unifiedPolyACoordsCount.toString());

                String polyAMatchType = match.Type.toString();
                rowStrings.add(polyAMatchType);

                String hasAnyPass = String.valueOf(variantHasAnyPass(oldBreakend) || variantHasAnyPass(newBreakend));
                rowStrings.add(hasAnyPass);

                RowValues rowValuesBreakendPair = new RowValues(oldBreakend, newBreakend);
                rowStrings.addAll(rowValuesBreakendPair.getAlternatingStrings());

                writer.write(String.join(TSV_DELIM, rowStrings));
                writer.newLine();
            }
            */

            writer.newLine();
            FileWriterUtils.closeBufferedWriter(writer);
        }
        catch(IOException e)
        {
            SV_LOGGER.error("Failed to write output file: {}", e.toString());
        }
    }
}
