package com.hartwig.hmftools.esvee.caller;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import static com.hartwig.hmftools.common.sv.StructuralVariantType.DEL;
import static com.hartwig.hmftools.common.sv.StructuralVariantType.DUP;
import static com.hartwig.hmftools.common.sv.StructuralVariantType.INS;
import static com.hartwig.hmftools.common.sv.StructuralVariantType.INV;
import static com.hartwig.hmftools.common.sv.StructuralVariantType.SGL;
import static com.hartwig.hmftools.common.sv.SvUtils.hasShortIndelLength;
import static com.hartwig.hmftools.common.sv.SvUtils.isIndel;
import static com.hartwig.hmftools.common.sv.SvVcfTags.AVG_FRAG_LENGTH;
import static com.hartwig.hmftools.common.sv.SvVcfTags.TOTAL_FRAGS;
import static com.hartwig.hmftools.common.utils.sv.StartEndIterator.SE_END;
import static com.hartwig.hmftools.common.utils.sv.StartEndIterator.SE_START;
import static com.hartwig.hmftools.common.variant.CommonVcfTags.PASS;

import java.util.Arrays;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.hmftools.common.genome.region.Orientation;
import com.hartwig.hmftools.common.sv.StructuralVariant;
import com.hartwig.hmftools.common.sv.StructuralVariantType;
import com.hartwig.hmftools.common.variant.GenotypeIds;
import com.hartwig.hmftools.esvee.caller.annotation.RepeatMaskAnnotation;
import com.hartwig.hmftools.esvee.common.FilterType;

import htsjdk.variant.variantcontext.VariantContext;

public class Variant
{
    // VCF data
    private final StructuralVariantType mType;

    private final Breakend[] mBreakends;

    // repeatedly used values for filtering are cached
    private final double mQual;
    private final String mInsertSequence;
    private boolean mIsShortLocalIndel;
    private int mPonCount;
    private boolean mIsHotspot;
    private boolean mGermline;
    private RepeatMaskAnnotation mRmAnnotation;
    private final Set<FilterType> mFilters;

    public Variant(final StructuralVariant sv, final GenotypeIds genotypeIds)
    {
        if(isIndel(sv.type()))
        {
            mIsShortLocalIndel = hasShortIndelLength(abs(sv.end().position() - sv.start().position()));
        }
        else
        {
            mIsShortLocalIndel = false;
        }

        mType = sv.type();

        Breakend breakendStart = Breakend.from(
                this, true, sv.start(), sv.startContext(), genotypeIds.ReferenceOrdinal, genotypeIds.TumorOrdinal);

        Breakend breakendEnd = sv.end() != null ?
                Breakend.from(this, false, sv.end(), sv.endContext(), genotypeIds.ReferenceOrdinal, genotypeIds.TumorOrdinal) : null;

        mBreakends = new Breakend[] { breakendStart, breakendEnd };

        mQual = sv.qualityScore();

        mInsertSequence = sv.insertSequence();

        mFilters = Sets.newHashSet();

        // keep any non-pass filters from assembly
        addExistingFilters(sv.startContext());
        addExistingFilters(sv.endContext());

        mPonCount = 0;
        mIsHotspot = false;
        mGermline = false;
        mRmAnnotation = null;
    }

    public String chromosomeStart() { return mBreakends[SE_START].Chromosome; }
    public String chromosomeEnd() { return !isSgl() ? mBreakends[SE_END].Chromosome : ""; }

    public int posStart() { return mBreakends[SE_START].Position; }
    public int posEnd() { return !isSgl() ? mBreakends[SE_END].Position : -1; }

    public Orientation orientStart() { return mBreakends[SE_START].Orient; }
    public Orientation orientEnd() { return !isSgl() ? mBreakends[SE_END].Orient : null; }

    public StructuralVariantType type() { return mType; }
    public boolean isSgl() { return mType == SGL; }

    public Breakend[] breakends() { return mBreakends; }
    public Breakend breakendStart() { return mBreakends[SE_START]; }
    public Breakend breakendEnd() { return mBreakends[SE_END]; }

    public VariantContext contextStart() { return mBreakends[SE_START].Context; }
    public VariantContext contextEnd() { return !isSgl() ? mBreakends[SE_END].Context : null; }

    public double qual() { return mQual; }
    public String insertSequence() { return mInsertSequence; }

    public int ponCount() { return mPonCount; }
    public void setPonCount(int count) { mPonCount = count; }

    public RepeatMaskAnnotation getRmAnnotation() { return mRmAnnotation; }
    public void setRepeatMaskAnnotation(final RepeatMaskAnnotation annotation) { mRmAnnotation = annotation; }

    public boolean isShortLocal() { return mIsShortLocalIndel; }

    public static boolean hasLength(final StructuralVariantType type)
    {
        return type == INS || type == INV || type == DEL || type == DUP;
    }

    public int svLength() { return hasLength(mType) ? abs(posEnd() - posStart()) : 0; }

    public int adjustedLength()
    {
        if(!hasLength(mType))
            return 0;

        if(mType == INS)
            return mInsertSequence.length();

        int positionLength = posEnd() - posStart();

        if(mType == DUP)
            ++positionLength;

        return positionLength + mInsertSequence.length();
    }

    public int averageFragmentLength() { return contextStart().getAttributeAsInt(AVG_FRAG_LENGTH, 0); }

    public int splitFragmentCount() { return contextStart().getAttributeAsInt(TOTAL_FRAGS, 0); }

    public void markHotspot() { mIsHotspot = true; }
    public boolean isHotspot() { return mIsHotspot; }

    public void markGermline() { mGermline = true; }
    public boolean isGermline() { return mGermline; }

    public boolean isLineSite()
    {
        for(Breakend breakend : mBreakends)
        {
            if(breakend != null && (breakend.isLine() || breakend.lineSiteBreakend() != null))
                return true;
        }

        return false;
    }

    public boolean inChainedAssembly()
    {
        for(Breakend breakend : mBreakends)
        {
            if(breakend != null && breakend.inChainedAssembly())
                return true;
        }

        return false;
    }

    public int maxUniqueFragmentPositions()
    {
        int maxUps = 0;
        for(Breakend breakend : mBreakends)
        {
            if(breakend != null)
                maxUps = max(maxUps, breakend.uniqueFragmentPositions());
        }

        return maxUps;
    }

    public void addFilter(final FilterType filter) { mFilters.add(filter); }
    public Set<FilterType> filters() { return mFilters; }
    public boolean isPass() { return mFilters.isEmpty(); }
    public boolean isFiltered() { return !mFilters.isEmpty(); }

    private void addExistingFilters(final VariantContext variantContext)
    {
        if(variantContext == null)
            return;

        // keep any non-pass filters from assembly
        for(String filterStr : variantContext.getFilters())
        {
            if(filterStr.equals(PASS))
                continue;

            FilterType filterType = Arrays.stream(FilterType.values()).filter(x -> x.vcfTag().equals(x)).findFirst().orElse(null);

            if(filterType != null)
                mFilters.add(filterType);
        }
    }

    public String toString()
    {
        if(!isSgl())
        {
            return String.format("%s pos(%s:%d - %s:%d)",
                    mType.toString(), chromosomeStart(), posStart(), chromosomeEnd(), posEnd());
        }
        else
        {
            return String.format("%s pos(%s:%d)",
                    mType.toString(), chromosomeStart(), posStart());
        }
    }
}
