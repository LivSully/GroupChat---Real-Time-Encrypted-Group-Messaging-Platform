package src;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EmojiRegistry {
    private static final Map<String, String> MAP;
    static {
        Map<String, String> m = new LinkedHashMap<>();

        // Smileys & emotion
        m.put("smile", "😊");
        m.put("grin", "😁");
        m.put("laugh", "😂");
        m.put("wink", "😉");
        m.put("blush", "😊");
        m.put("cool", "😎");
        m.put("thinking", "🤔");
        m.put("cry", "😭");
        m.put("angry", "😠");
        m.put("rage", "😡");
        m.put("scream", "😱");
        m.put("surprised", "😲");
        m.put("yawn", "🥱");
        m.put("sleep", "😴");
        m.put("nerd", "🤓");
        m.put("skull", "💀");
        m.put("explode", "🤯");
        m.put("shush", "🤫");
        m.put("monocle", "🧐");
        m.put("zipper", "🤐");
        m.put("salute", "🫡");
        m.put("party", "🥳");
        m.put("hot", "🥵");
        m.put("cold", "🥶");
        m.put("thumbsup", "👍");
        m.put("thumbsdown", "👎");
        m.put("wave", "👋");
        m.put("clap", "👏");
        m.put("pray", "🙏");
        m.put("point", "👉");
        m.put("ok", "👌");
        m.put("fist", "✊");
        m.put("shrug", "🤷");
        m.put("heart", "❤️");
        m.put("broken_heart", "💔");

        MAP = Collections.unmodifiableMap(m);
    }

    // Pattern to find :shortcode: in stored messages for rendering
    private static final Pattern SHORTCODE_PATTERN = Pattern.compile(":([a-z0-9_]+):");

    // Reverse map for encoding raw emoji back to shortcodes
    private static final Map<String, String> REVERSE_MAP;
    static {
        Map<String, String> r = new LinkedHashMap<>();
        MAP.forEach((code, emoji) -> r.putIfAbsent(emoji, ":" + code + ":"));
        REVERSE_MAP = Collections.unmodifiableMap(r);
    }

    // make emoji registry non-instantiable since it only has static methods and
    // data
    private EmojiRegistry() {
    }

    // Expose the map for testing purposes, but it should not be modified at runtime
    public static Map<String, String> getMap() {
        return MAP;
    }

    // Render a stored message by replacing :shortcode: with the corresponding emoji
    // character.
    public static String render(String text) {
        if (text == null || text.isEmpty())
            return text;
        Matcher m = SHORTCODE_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String code = m.group(1);
            String emoji = MAP.get(code);
            m.appendReplacement(sb, emoji != null ? Matcher.quoteReplacement(emoji) : m.group());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    // Encode raw text by replacing any emoji characters with their :shortcode:
    // equivalents for storage/transmission.
    public static String encode(String text) {
        if (text == null || text.isEmpty())
            return text;
        // We scan the text character by character, trying to match the longest possible
        // emoji at each position.
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            // Try longest-possible emoji match first (up to 8 chars covers ZWJ sequences)
            boolean matched = false;
            int maxLen = Math.min(8, text.length() - i);
            for (int len = maxLen; len >= 1; len--) {
                String candidate = text.substring(i, i + len);
                String code = REVERSE_MAP.get(candidate);
                if (code != null) {
                    sb.append(code);
                    i += len;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                sb.append(text.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }

    // Check if a shortcode is known (for validation purposes)
    public static boolean isKnown(String shortcode) {
        return MAP.containsKey(shortcode);
    }

    // Get the emoji character for a given shortcode, or null if not found

    public static String getEmoji(String shortcode) {
        return MAP.get(shortcode);
    }
}
