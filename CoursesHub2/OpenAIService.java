package CoursesHub2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * AI-backed survey & recommendations.
 * - If OPENAI_API_KEY is set: uses OpenAI to generate interest questions (English-only),
 *   with options derived from *actual* courses in DB (passed from UI).
 *   Labels are plain English (no parentheses/brackets, no course names), but each option
 *   maps internally to a course_name from the provided list.
 * - If missing API key or error: falls back to local heuristic category-based generator.
 */
public class OpenAIService {

    // ===== Models =====
    public static class InterestQuestion {
        public String question;
        public List<Option> options = new ArrayList<>();
        public InterestQuestion(String q){ this.question=q; }
        public static class Option {
            public String label;      // what the student sees (English text, no course names)
            public String courseName; // representative course behind this option (must be from DB list)
            public Option(String label, String courseName){ this.label=label; this.courseName=courseName; }
        }
    }

    //Config
    private final String apiKey = System.getenv("OPENAI_API_KEY");
    // You can change the model to "gpt-4o" if you want higher quality.
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_MODEL = "gpt-4o-mini";

    public boolean isConfigured() { return apiKey != null && !apiKey.trim().isEmpty(); }

    // Public API

    /** Generate 6..12 interest questions in EN, options mapped to courses from DB */
    public List<InterestQuestion> generateInterestSurvey(String academyName,
                                                         List<String> courses,
                                                         int totalQuestions) {
        if (courses == null) courses = Collections.emptyList();

        // If AI is configured, try OpenAI first
        if (isConfigured()) {
            try {
                int qCount = clamp(totalQuestions, 6, 12);
                return callOpenAIForSurvey(academyName, courses, qCount);
            } catch (Exception ex) {
                ex.printStackTrace();
                // fall through to local generator
            }
        }

        // Fallback: local heuristic (categories)
        return localGenerateSurvey(courses, clamp(totalQuestions, 6, 12));
    }

    /** Ask AI to produce ranked recommendations text (EN). Fallback: deterministic. */
    public String recommendCourses(String academyName,
                                   Map<String, Double> interestPercent,
                                   List<String> catalogLines) {
        if (interestPercent == null || interestPercent.isEmpty()) {
            return "Not enough answers to recommend courses yet.";
        }

        if (isConfigured()) {
            try {
                return callOpenAIForAdvice(academyName, interestPercent, catalogLines);
            } catch (Exception ex) {
                ex.printStackTrace();
                // fall back
            }
        }

        // Fallback deterministic
        List<Map.Entry<String, Double>> list = new ArrayList<>(interestPercent.entrySet());
        list.sort((a,b) -> Double.compare(b.getValue(), a.getValue()));
        List<Map.Entry<String, Double>> top = list.subList(0, Math.min(3, list.size()));

        StringBuilder sb = new StringBuilder(900);
        sb.append("Recommendations for ")
          .append(academyName == null ? "this academy" : academyName)
          .append(":\n\n");
        int rank = 1;
        for (Map.Entry<String, Double> e : top) {
            String cn = e.getKey();
            sb.append(rank++).append(") ").append(cn)
              .append(" — interest: ")
              .append(String.format(Locale.US, "%.1f%%", e.getValue()))
              .append("\n");
            String line = findCatalogLine(catalogLines, cn);
            if (line != null) sb.append("   • ").append(line).append("\n");
        }
        sb.append("\nTips:\n")
          .append("• Start with #1 to match your strongest interests.\n")
          .append("• If schedule/level doesn’t fit, try #2 or #3.\n")
          .append("• Retake the survey anytime to refine results.\n");
        return sb.toString();
    }

    //
    //  OPENAI: Survey Generation (JSON)  
  

    /** Calls OpenAI to generate JSON survey from actual DB course list. */
    private List<InterestQuestion> callOpenAIForSurvey(String academyName,
                                                       List<String> courses,
                                                       int totalQuestions) throws Exception {
        // Build a concise system & user prompt.
        JSONObject req = new JSONObject();
        req.put("model", OPENAI_MODEL);

        JSONArray messages = new JSONArray();

        // System: strict instructions about format and style
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content",
                        "You generate an English interest survey for students choosing courses.\n" +
                        "You MUST return ONLY a strict JSON object with this schema:\n" +
                        "{ \"questions\": [ { \"question\": String, \"options\": [ {\"label\": String, \"course_name\": String} ... ] } ... ] }\n" +
                        "- `label`: English text, plain. NO parentheses, brackets, or course names. No bullets.\n" +
                        "- `course_name`: MUST be one of the provided course names (exact string).\n" +
                        "- Use 3 or 4 options per question.\n" +
                        "- Use between 6 and 12 questions (as requested).\n" +
                        "- Options should map to different course 'themes' inferred from provided courses.\n" +
                        "- Keep language clear and simple; labels describe interests/tasks, not course titles.\n" +
                        "- Do not add any extra keys or commentary outside the JSON." ));

        // User: pass academy, course list, and requested count
        JSONObject payload = new JSONObject();
        payload.put("academy_name", academyName == null ? "" : academyName);
        payload.put("total_questions", totalQuestions);
        JSONArray arr = new JSONArray();
        for (String c : courses) arr.put(c);
        payload.put("courses", arr);

        messages.put(new JSONObject()
                .put("role", "user")
                .put("content",
                        "Generate the survey now from these courses (use them only to derive themes and to fill `course_name` for each option):\n" +
                        payload.toString()));

        req.put("messages", messages);
        req.put("temperature", 0.4);

        String response = httpPostJson(OPENAI_URL, req.toString(), apiKey);
        // Parse chat completion
        JSONObject root = new JSONObject(response);
        JSONArray choices = root.optJSONArray("choices");
        if (choices == null || choices.length() == 0)
            throw new RuntimeException("No choices from OpenAI.");

        String content = choices.getJSONObject(0)
                .getJSONObject("message")
                .optString("content", "").trim();

        if (content.isEmpty()) throw new RuntimeException("Empty content from OpenAI.");

        // Sometimes model may wrap in markdown; try to extract JSON block
        String jsonText = extractJson(content);
        if (jsonText == null) jsonText = content;

        return parseSurveyJson(jsonText, new HashSet<>(courses));
    }

    /** Parse JSON string into InterestQuestion list, validating course_name values. */
    private List<InterestQuestion> parseSurveyJson(String json, Set<String> allowedCourses) {
        List<InterestQuestion> out = new ArrayList<>();
        JSONObject obj = new JSONObject(json);
        JSONArray qArr = obj.getJSONArray("questions");
        for (int i=0; i<qArr.length(); i++) {
            JSONObject qObj = qArr.getJSONObject(i);
            String q = qObj.getString("question");
            InterestQuestion iq = new InterestQuestion(q);

            JSONArray opts = qObj.getJSONArray("options");
            for (int j=0; j<opts.length(); j++) {
                JSONObject o = opts.getJSONObject(j);
                String label = o.getString("label");
                String courseName = o.getString("course_name");

                // Clean label: enforce no parentheses/brackets (defensive)
                label = label.replace("(", "").replace(")", "")
                             .replace("[", "").replace("]", "").trim();

                // Validate course
                if (!allowedCourses.contains(courseName)) {
                    // If invalid, skip this option
                    continue;
                }
                iq.options.add(new InterestQuestion.Option(label, courseName));
            }
            // Keep questions with at least 1 option
            if (!iq.options.isEmpty()) out.add(iq);
        }
        return out;
    }

    //
    //  OPENAI: Recommendations (Text) 
    //

    /** Calls OpenAI to write a short English advisory using interest% + catalog. */
    private String callOpenAIForAdvice(String academyName,
                                       Map<String, Double> interestPercent,
                                       List<String> catalogLines) throws Exception {
        JSONObject req = new JSONObject();
        req.put("model", OPENAI_MODEL);

        JSONArray messages = new JSONArray();

        messages.put(new JSONObject()
                .put("role", "system")
                .put("content",
                        "You are a concise academic advisor. Write in clear English.\n" +
                        "Use the student's interest scores (by course name) and the academy's course catalog.\n" +
                        "Return a short recommendation:\n" +
                        "- Start with a 1–2 sentence summary.\n" +
                        "- Then list Top-3 course recommendations as numbered lines with brief reasons.\n" +
                        "- Use exact course names from the catalog or the score keys.\n" +
                        "- Keep it under 120 words, no marketing fluff." ));

        JSONObject data = new JSONObject();
        data.put("academy_name", academyName == null ? "" : academyName);

        JSONObject scores = new JSONObject();
        for (Map.Entry<String, Double> e : interestPercent.entrySet()) {
            scores.put(e.getKey(), e.getValue());
        }
        data.put("interest_percent", scores);

        JSONArray cat = new JSONArray();
        if (catalogLines != null) for (String s : catalogLines) cat.put(s);
        data.put("catalog", cat);

        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", "Recommend courses based on this JSON:\n" + data.toString()));

        req.put("messages", messages);
        req.put("temperature", 0.4);

        String response = httpPostJson(OPENAI_URL, req.toString(), apiKey);
        JSONObject root = new JSONObject(response);
        JSONArray choices = root.optJSONArray("choices");
        if (choices == null || choices.length() == 0)
            throw new RuntimeException("No choices from OpenAI.");

        String content = choices.getJSONObject(0)
                .getJSONObject("message")
                .optString("content", "").trim();

        if (content.isEmpty()) return "No advice returned.";
        return content;
    }
 
    // Local Heuristic Fallback  

    private enum Category {
        NETWORKING_INFRA, AI_DATA, SECURITY, UI_MOBILE_WEB, CLOUD
    }

    // Keywords per category (lowercase)
    private static final String[] K_NET  = {"network","ccna","routing","switch","infrastructure","devops","linux"};
    private static final String[] K_AI   = {"ai","ml","machine","data","analytics","pytorch","tensorflow","scikit","numpy"};
    private static final String[] K_SEC  = {"security","cyber","oscp","ceh","kali","siem","pentest","hacking","forensic"};
    private static final String[] K_UI   = {"mobile","android","ios","flutter","web","frontend","ui","ux","react","swift","compose"};
    private static final String[] K_CLOUD= {"cloud","aws","azure","gcp","kubernetes","docker"};

    private List<InterestQuestion> localGenerateSurvey(List<String> courses, int totalQuestions) {
        LinkedHashMap<Category, String> cat2rep = classifyAndPickRepresentatives(courses);

        if (cat2rep.isEmpty()) return Collections.emptyList();

        List<InterestQuestion> out = new ArrayList<>();
        for (int i=0; i<totalQuestions; i++) {
            String prompt = PROMPTS[i % PROMPTS.length];
            InterestQuestion q = new InterestQuestion(prompt);
            int pIndex = i % PROMPTS.length;
            for (Map.Entry<Category,String> en : cat2rep.entrySet()) {
                Category c = en.getKey();
                String rep = en.getValue();
                String label = labelFor(c, pIndex);
                q.options.add(new InterestQuestion.Option(label, rep));
            }
            out.add(q);
        }
        return out;
    }

    private LinkedHashMap<Category,String> classifyAndPickRepresentatives(List<String> courses) {
        LinkedHashMap<Category,String> map = new LinkedHashMap<>();
        for (String c : courses) {
            if (c == null) continue;
            String name = c.trim();
            String l = name.toLowerCase(Locale.ENGLISH);

            if (!map.containsKey(Category.NETWORKING_INFRA) && anyMatch(l, K_NET))  { map.put(Category.NETWORKING_INFRA, name); continue; }
            if (!map.containsKey(Category.AI_DATA)          && anyMatch(l, K_AI))   { map.put(Category.AI_DATA, name);          continue; }
            if (!map.containsKey(Category.SECURITY)         && anyMatch(l, K_SEC))  { map.put(Category.SECURITY, name);         continue; }
            if (!map.containsKey(Category.UI_MOBILE_WEB)    && anyMatch(l, K_UI))   { map.put(Category.UI_MOBILE_WEB, name);    continue; }
            if (!map.containsKey(Category.CLOUD)            && anyMatch(l, K_CLOUD)){ map.put(Category.CLOUD, name);            continue; }
        }
        // Fill up to 4 categories by guessing
        for (String c : courses) {
            if (map.size() >= 4) break;
            String l = c == null ? "" : c.toLowerCase(Locale.ENGLISH);
            Category guessed = guessCategory(l);
            if (!map.containsKey(guessed)) map.put(guessed, c);
        }
        // limit to max 4 categories
        LinkedHashMap<Category,String> limited = new LinkedHashMap<>();
        int i=0; for (Map.Entry<Category,String> e: map.entrySet()) {
            limited.put(e.getKey(), e.getValue());
            if (++i==4) break;
        }
        return limited;
    }

    private boolean anyMatch(String l, String[] keys) { for (String k: keys) if (l.contains(k)) return true; return false; }
    private Category guessCategory(String l) {
        if (anyMatch(l, K_NET))  return Category.NETWORKING_INFRA;
        if (anyMatch(l, K_AI))   return Category.AI_DATA;
        if (anyMatch(l, K_SEC))  return Category.SECURITY;
        if (anyMatch(l, K_UI))   return Category.UI_MOBILE_WEB;
        if (anyMatch(l, K_CLOUD))return Category.CLOUD;
        return Category.NETWORKING_INFRA;
    }

    private static final String[] PROMPTS = new String[]{
            "Which kind of work sounds most exciting to you?",
            "Which challenge would you rather solve first?",
            "Which tools/technologies are you most curious about?",
            "How do you feel about math/statistics in daily work?",
            "What work environment attracts you the most?",
            "Pick a one-month project you’d enjoy shipping",
            "Which topic would you dive into next?",
            "What best describes your day-to-day interest?",
            "Where do you want to grow your skills next?",
            "Which type of problems feel most satisfying to fix?",
            "What kind of collaboration sounds fun to you?",
            "Which path aligns better with your career goals?"
    };

    private String labelFor(Category c, int promptIndex) {
        switch (c) {
            case NETWORKING_INFRA: return labelNet(promptIndex);
            case AI_DATA:          return labelAI(promptIndex);
            case SECURITY:         return labelSec(promptIndex);
            case UI_MOBILE_WEB:    return labelUI(promptIndex);
            case CLOUD:            return labelCloud(promptIndex);
            default:               return "Hands-on systems and reliability";
        }
    }

    private String labelNet(int i) {
        switch (i % 6) {
            case 0: return "Configuring infrastructure and keeping systems stable";
            case 1: return "Troubleshooting connectivity and performance";
            case 2: return "Networking & infrastructure stacks";
            case 3: return "Practical systems work over heavy math";
            case 4: return "Infrastructure labs and deployment";
            default:return "Design and deploy reliable foundations";
        }
    }
    private String labelAI(int i) {
        switch (i % 6) {
            case 0: return "Building intelligent features or analyzing data";
            case 1: return "Prediction/classification using real data";
            case 2: return "Python/ML frameworks and analytics";
            case 3: return "Enjoy math/stats and model thinking";
            case 4: return "Data experiments and notebooks";
            default:return "Train/evaluate small ML models";
        }
    }
    private String labelSec(int i) {
        switch (i % 6) {
            case 0: return "Securing systems and finding vulnerabilities";
            case 1: return "Investigating cyber incidents and threats";
            case 2: return "Security toolkits and blue/red-team practice";
            case 3: return "Logic/policy focus over math-heavy work";
            case 4: return "Security operations and incident response";
            default:return "Pen-testing and hardening environments";
        }
    }
    private String labelUI(int i) {
        switch (i % 6) {
            case 0: return "Designing and delivering user-facing applications";
            case 1: return "Crafting clean UI flows and great UX";
            case 2: return "Front-end/mobile frameworks and APIs";
            case 3: return "Light math—strong product & design focus";
            case 4: return "Product sprints and prototyping";
            default:return "Ship an MVP app users can try";
        }
    }
    private String labelCloud(int i) {
        switch (i % 6) {
            case 0: return "Orchestrating scalable cloud services";
            case 1: return "Automating deployments and observability";
            case 2: return "Kubernetes/Docker & cloud platforms";
            case 3: return "Systems thinking with practical math";
            case 4: return "Platforms, CI/CD, and reliability";
            default:return "Design resilient cloud architectures";
        }
    }
    // HTTP Utils 

    private String httpPostJson(String url, String body, String apiKey) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);

        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(body.getBytes(StandardCharsets.UTF_8));
            wr.flush();
        }

        int code = conn.getResponseCode();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream(),
                StandardCharsets.UTF_8
        ));
        StringBuilder sb = new StringBuilder();
        String line; while ((line = br.readLine()) != null) sb.append(line).append('\n');
        br.close();
        if (code < 200 || code >= 300) {
            throw new RuntimeException("OpenAI HTTP " + code + ": " + sb.toString());
        }
        return sb.toString();
    }

    /** Extract first JSON object from possibly wrapped text. */
    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1).trim();
        }
        return null;
    }

    //  Helpers

    private int clamp(int val, int min, int max) { return Math.max(min, Math.min(max, val)); }

    private String findCatalogLine(List<String> catalog, String courseName) {
        if (catalog == null || courseName == null) return null;
        String nameL = courseName.toLowerCase(Locale.ENGLISH);
        for (String line : catalog) {
            if (line == null) continue;
            if (line.toLowerCase(Locale.ENGLISH).startsWith(nameL)) return line;
        }
        return null;
    }
}
