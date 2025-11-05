package libc;
import com.sun.jna.*;
import com.sun.jna.ptr.*;
// JNA mapping to libc
public interface LibC extends Library {
  // timeval structure (used by select)
    public static class Timeval extends Structure {
        public NativeLong tv_sec;   // seconds
        public NativeLong tv_usec;  // microseconds

        @Override
        protected java.util.List<String> getFieldOrder() {
            return java.util.Arrays.asList("tv_sec", "tv_usec");
        }
    }

    // fd_set structure
    public static class FDSet extends Structure {
        private static final int FD_SETSIZE = 1024;
        //public int[] fds_bits = new int[(FD_SETSIZE + 31) / 32];
	
        public long[] fds_bits = new long[FD_SETSIZE /64];
	

        @Override
        protected java.util.List<String> getFieldOrder() {
            return java.util.Arrays.asList("fds_bits");
        }

        // Helper methods to manipulate fd_set
        public void FD_ZERO() {
            for (int i = 0; i < fds_bits.length; i++) fds_bits[i] = 0;
        }

        public void FD_SET(long fd) {	    
            int idx = (int)fd/64;
            fds_bits[idx] |= (1 << (fd % 64));
        }

        public boolean FD_ISSET(long fd) {
            return (fds_bits[(int)(fd / 64)] & (1 << (fd % 64))) != 0;
        }
    }
  public final LibC INSTANCE = Native.load("c", LibC.class);
  int select(long nfds, FDSet readfds, Pointer writefds, Pointer exceptfds, Timeval timeout);
}
