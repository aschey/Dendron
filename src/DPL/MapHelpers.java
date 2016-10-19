package DPL;

import java.util.HashMap;

/**
 * Created by aschey on 10/18/16.
 */
public class MapHelpers {
    static <T1, T2> HashMap<T1, T2> initialize(Object... args) {
        HashMap<T1, T2> result = new HashMap<>();
        for (int i = 0; i < args.length - 1; i += 2) {
            result.put((T1)args[i], (T2)args[i + 1]);
        }

        return result;
    }
}
