package clAmsMgmtClientApi;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;


public class ClListHeadT extends Structure {

    public Pointer pNext;   // struct ClListHead *
    public Pointer pPrev;   // struct ClListHead *

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("pNext", "pPrev");
    }

    public static class ByReference extends ClListHeadT
            implements Structure.ByReference {}

    public static class ByValue extends ClListHeadT
            implements Structure.ByValue {}
}

