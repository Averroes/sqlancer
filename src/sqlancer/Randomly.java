package sqlancer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public final class Randomly {

    private static final boolean USE_CACHING = true;
    private static final int CACHE_SIZE = 100;

    private final List<Long> cachedLongs = new ArrayList<>();
    private final List<String> cachedStrings = new ArrayList<>();
    private final List<Double> cachedDoubles = new ArrayList<>();
    private final List<byte[]> cachedBytes = new ArrayList<>();
    private static final String ALPHABET = new String(
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzöß!#<>/.,~-+'*()[]{} ^*?%_\t\n\r|&\\");
    private Supplier<String> provider;

    private static final ThreadLocal<Random> THREAD_RANDOM = new ThreadLocal<>();

    private void addToCache(long val) {
        if (USE_CACHING && cachedLongs.size() < CACHE_SIZE && !cachedLongs.contains(val)) {
            cachedLongs.add(val);
        }
    }

    private void addToCache(double val) {
        if (USE_CACHING && cachedDoubles.size() < CACHE_SIZE && !cachedDoubles.contains(val)) {
            cachedDoubles.add(val);
        }
    }

    private void addToCache(String val) {
        if (USE_CACHING && cachedStrings.size() < CACHE_SIZE && !cachedStrings.contains(val)) {
            cachedStrings.add(val);
        }
    }

    private void addToCache(byte[] val) {
        if (USE_CACHING && cachedBytes.size() < CACHE_SIZE && !cachedBytes.contains(val)) {
            cachedBytes.add(val);
        }
    }

    private byte[] getFromBytesCache() {
        if (!USE_CACHING || cachedBytes.isEmpty()) {
            return null;
        } else {
            byte[] bytes = Randomly.fromList(cachedBytes);
            if (Randomly.getBoolean()) {
                for (int i = 0; i < Randomly.smallNumber(); i++) {
                    bytes[getInteger(0, bytes.length)] = (byte) THREAD_RANDOM.get().nextInt();
                }
            }
            return bytes;
        }
    }

    private Long getFromLongCache() {
        if (!USE_CACHING || cachedLongs.isEmpty()) {
            return null;
        } else {
            return Randomly.fromList(cachedLongs);
        }
    }

    private Double getFromDoubleCache() {
        if (!USE_CACHING) {
            return null;
        }
        if (Randomly.getBoolean() && !cachedLongs.isEmpty()) {
            return (double) Randomly.fromList(cachedLongs);
        } else if (!cachedDoubles.isEmpty()) {
            return Randomly.fromList(cachedDoubles);
        } else {
            return null;
        }
    }

    private String getFromStringCache() {
        if (!USE_CACHING) {
            return null;
        }
        if (Randomly.getBoolean() && !cachedLongs.isEmpty()) {
            return String.valueOf(Randomly.fromList(cachedLongs));
        } else if (Randomly.getBoolean() && !cachedDoubles.isEmpty()) {
            return String.valueOf(Randomly.fromList(cachedDoubles));
        } else if (Randomly.getBoolean() && !cachedBytes.isEmpty()) {
            return new String(Randomly.fromList(cachedBytes));
        } else if (!cachedStrings.isEmpty()) {
            String randomString = Randomly.fromList(cachedStrings);
            if (Randomly.getBoolean()) {
                return randomString;
            } else {
                if (Randomly.getBoolean()) {
                    return randomString.toLowerCase();
                } else if (Randomly.getBoolean()) {
                    return randomString.toUpperCase();
                } else {
                    char[] chars = randomString.toCharArray();
                    if (chars.length != 0) {
                        for (int i = 0; i < Randomly.smallNumber(); i++) {
                            chars[getInteger(0, chars.length)] = ALPHABET.charAt(getInteger(0, ALPHABET.length()));
                        }
                    }
                    return new String(chars);
                }
            }
        } else {
            return null;
        }
    }

    private static boolean cacheProbability() {
        return USE_CACHING && getNextLong(0, 3) == 1;
    }

    // CACHING END

    public static <T> T fromList(List<T> list) {
        return list.get((int) getNextLong(0, list.size()));
    }

    @SafeVarargs
    public static <T> T fromOptions(T... options) {
        return options[getNextInt(0, options.length)];
    }

    @SafeVarargs
    public static <T> List<T> nonEmptySubset(T... options) {
        int nr = 1 + getNextInt(0, options.length);
        return extractNrRandomColumns(Arrays.asList(options), nr);
    }

    public static <T> List<T> nonEmptySubset(List<T> columns) {
        int nr = 1 + getNextInt(0, columns.size());
        return nonEmptySubset(columns, nr);
    }

    public static <T> List<T> nonEmptySubset(List<T> columns, int nr) {
        if (nr > columns.size()) {
            throw new AssertionError(columns + " " + nr);
        }
        return extractNrRandomColumns(columns, nr);
    }

    public static <T> List<T> nonEmptySubsetPotentialDuplicates(List<T> columns) {
        List<T> arr = new ArrayList<>();
        for (int i = 0; i < Randomly.smallNumber() + 1; i++) {
            arr.add(Randomly.fromList(columns));
        }
        return arr;
    }

    public static <T> List<T> subset(List<T> columns) {
        int nr = getNextInt(0, columns.size() + 1);
        return extractNrRandomColumns(columns, nr);
    }

    public static <T> List<T> subset(int nr, @SuppressWarnings("unchecked") T... values) {
        List<T> list = new ArrayList<>();
        for (T val : values) {
            list.add(val);
        }
        return extractNrRandomColumns(list, nr);
    }

    public static <T> List<T> subset(@SuppressWarnings("unchecked") T... values) {
        List<T> list = new ArrayList<>();
        for (T val : values) {
            list.add(val);
        }
        return subset(list);
    }

    public static <T> List<T> extractNrRandomColumns(List<T> columns, int nr) {
        assert nr >= 0;
        List<T> selectedColumns = new ArrayList<>();
        List<T> remainingColumns = new ArrayList<>(columns);
        for (int i = 0; i < nr; i++) {
            selectedColumns.add(remainingColumns.remove(getNextInt(0, remainingColumns.size())));
        }
        return selectedColumns;
    }

    public static int smallNumber() {
        // no need to cache for small numbers
        return (int) (Math.abs(THREAD_RANDOM.get().nextGaussian()) * 2);
    }

    public static boolean getBoolean() {
        return THREAD_RANDOM.get().nextBoolean();
    }

    public long getInteger() {
        if (smallBiasProbability()) {
            return Randomly.fromOptions(-1L, Long.MAX_VALUE, Long.MIN_VALUE, 1L, 0L);
        } else {
            if (cacheProbability()) {
                Long l = getFromLongCache();
                if (l != null) {
                    return l;
                }
            }
            long nextLong = THREAD_RANDOM.get().nextInt();
            addToCache(nextLong);
            return nextLong;
        }
    }

    public String getString() {
        if (smallBiasProbability()) {
            return Randomly.fromOptions("TRUE", "FALSE", "0.0", "-0.0", "1e500", "-1e500");
        }
        if (cacheProbability()) {
            String s = getFromStringCache();
            if (s != null) {
                return s;
            }
        }

        int n = ALPHABET.length();

        StringBuilder sb = new StringBuilder();

        int chars;
        if (Randomly.getBoolean()) {
            chars = Randomly.smallNumber();
        } else {
            chars = getInteger(0, 30);
        }
        for (int i = 0; i < chars; i++) {
            if (Randomly.getBooleanWithRatherLowProbability()) {
                char val = (char) getInteger();
                if (val != 0) {
                    sb.append(val);
                }
            } else {
                sb.append(ALPHABET.charAt(getNextInt(0, n)));
            }
        }
        while (Randomly.getBooleanWithSmallProbability()) {
            String[][] pairs = { { "{", "}" }, { "[", "]" }, { "(", ")" } };
            int idx = (int) Randomly.getNotCachedInteger(0, pairs.length);
            int left = (int) Randomly.getNotCachedInteger(0, sb.length() + 1);
            sb.insert(left, pairs[idx][0]);
            int right = (int) Randomly.getNotCachedInteger(left + 1, sb.length() + 1);
            sb.insert(right, pairs[idx][1]);
        }
        if (provider != null) {
            while (Randomly.getBooleanWithSmallProbability()) {
                if (sb.length() == 0) {
                    sb.append(provider.get());
                } else {
                    sb.insert((int) Randomly.getNotCachedInteger(0, sb.length()), provider.get());
                }
            }
        }

        String s = sb.toString();

        addToCache(s);
        return s;
    }

    public byte[] getBytes() {
        if (cacheProbability()) {
            byte[] val = getFromBytesCache();
            if (val != null) {
                addToCache(val);
                return val;
            }
        }
        int size = Randomly.smallNumber();
        byte[] arr = new byte[size];
        THREAD_RANDOM.get().nextBytes(arr);
        return arr;
    }

    public long getNonZeroInteger() {
        long value;
        if (smallBiasProbability()) {
            return Randomly.fromOptions(-1L, Long.MAX_VALUE, Long.MIN_VALUE, 1L);
        }
        if (cacheProbability()) {
            Long l = getFromLongCache();
            if (l != null && l != 0) {
                return l;
            }
        }
        do {
            value = getInteger();
        } while (value == 0);
        assert value != 0;
        addToCache(value);
        return value;
    }

    public long getPositiveInteger() {
        if (cacheProbability()) {
            Long value = getFromLongCache();
            if (value != null && value >= 0) {
                return value;
            }
        }
        long value;
        if (smallBiasProbability()) {
            value = Randomly.fromOptions(0L, Long.MAX_VALUE, 1L);
        } else {
            value = getNextLong(0, Long.MAX_VALUE);
        }
        addToCache(value);
        assert value >= 0;
        return value;
    }

    public double getFiniteDouble() {
        while (true) {
            double val = getDouble();
            if (Double.isFinite(val)) {
                return val;
            }
        }
    }

    public double getDouble() {
        if (smallBiasProbability()) {
            return Randomly.fromOptions(0.0, -0.0, Double.MAX_VALUE, -Double.MAX_VALUE, Double.POSITIVE_INFINITY,
                    Double.NEGATIVE_INFINITY);
        } else if (cacheProbability()) {
            Double d = getFromDoubleCache();
            if (d != null) {
                return d;
            }
        }
        double value = THREAD_RANDOM.get().nextDouble();
        addToCache(value);
        return value;
    }

    private static boolean smallBiasProbability() {
        return THREAD_RANDOM.get().nextInt(100) == 1;
    }

    public static boolean getBooleanWithRatherLowProbability() {
        return THREAD_RANDOM.get().nextInt(10) == 1;
    }

    public static boolean getBooleanWithSmallProbability() {
        return smallBiasProbability();
    }

    public int getInteger(int left, int right) {
        if (left == right) {
            return left;
        }
        return (int) getLong(left, right);
    }

    // TODO redundant?
    public long getLong(long left, long right) {
        if (left == right) {
            return left;
        }
        return getLong(left, right);
    }

    public BigDecimal getRandomBigDecimal() {
        return new BigDecimal(THREAD_RANDOM.get().nextDouble());
    }

    public long getPositiveIntegerNotNull() {
        while (true) {
            long val = getPositiveInteger();
            if (val != 0) {
                return val;
            }
        }
    }

    public static long getNonCachedInteger() {
        return THREAD_RANDOM.get().nextLong();
    }

    public static long getPositiveNonCachedInteger() {
        return getNextLong(1, Long.MAX_VALUE);
    }

    public static long getPositiveOrZeroNonCachedInteger() {
        return getNextLong(0, Long.MAX_VALUE);
    }

    public static long getNotCachedInteger(int lower, int upper) {
        return getNextLong(lower, upper);
    }

    public Randomly(Supplier<String> provider) {
        this.provider = provider;
    }

    public Randomly() {
        THREAD_RANDOM.set(new Random());
    }

    public Randomly(long seed) {
        THREAD_RANDOM.set(new Random(seed));
    }

    public static double getUncachedDouble() {
        return THREAD_RANDOM.get().nextDouble();
    }

    public String getChar() {
        while (true) {
            String s = getString();
            if (!s.isEmpty()) {
                return s.substring(0, 1);
            }
        }
    }

    // see https://stackoverflow.com/a/2546158
    // uniformity does not seem to be important for us
    // SQLancer previously used ThreadLocalRandom.current().nextLong(lower, upper)
    private static long getNextLong(long lower, long upper) {
        return lower + ((long) (THREAD_RANDOM.get().nextDouble() * (upper - lower)));
    }

    private static int getNextInt(int lower, int upper) {
        return (int) getNextLong(lower, upper);
    }

}
