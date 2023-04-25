package org.example.Utils;

public class SeqUtils {
    private static SeqUtils seqUtils;
    private long value;

    private SeqUtils(){
        value = 0;
    }

    public long getValue(){
        return  value;
    }

    public static SeqUtils getSeqUtils(){
        if (seqUtils == null)
            synchronized (SeqUtils.class){
            if (seqUtils == null)
                seqUtils = new SeqUtils();
            }
        return seqUtils;
    }

    public void accumulate(){
        ++value;
    }

}
