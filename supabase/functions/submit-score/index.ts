const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Content-Type": "application/json",
};

function json(data: unknown, status = 200) {
  return new Response(JSON.stringify(data), { status, headers: corsHeaders });
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return json({ error: "method_not_allowed" }, 405);

  try {
    const suppliedKey = req.headers.get("apikey") ?? "";
    const publishableRaw = Deno.env.get("SUPABASE_PUBLISHABLE_KEYS") ?? "{}";
    const publishable = Object.values(JSON.parse(publishableRaw)) as string[];

    if (!suppliedKey || !publishable.includes(suppliedKey)) {
      return json({ error: "unauthorized" }, 401);
    }

    const body = await req.json();
    const nickname = String(body?.nickname ?? "")
      .replace(/[\u0000-\u001f\u007f]/g, "")
      .trim()
      .slice(0, 14);
    const score = Number(body?.score);
    const kills = Number(body?.kills ?? 0);
    const durationMs = Number(body?.duration_ms);
    const appVersion = String(body?.app_version ?? "1.0.0").trim().slice(0, 32);

    if (!nickname || !Number.isInteger(score) || score < 0 || score > 1_000_000) {
      return json({ error: "invalid_score" }, 400);
    }
    if (!Number.isInteger(kills) || kills < 0 || kills > 10_000) {
      return json({ error: "invalid_kills" }, 400);
    }
    if (!Number.isInteger(durationMs) || durationMs < 1_000 || durationMs > 21_600_000) {
      return json({ error: "invalid_duration" }, 400);
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL");
    const modernSecrets = Deno.env.get("SUPABASE_SECRET_KEYS");
    const legacyServiceRole = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
    const secretKey = modernSecrets
      ? (Object.values(JSON.parse(modernSecrets))[0] as string | undefined)
      : legacyServiceRole;

    if (!supabaseUrl || !secretKey) return json({ error: "server_misconfigured" }, 500);

    const headers: Record<string, string> = {
      apikey: secretKey,
      "Content-Type": "application/json",
      Prefer: "return=minimal",
    };
    if (!secretKey.startsWith("sb_secret_")) {
      headers.Authorization = `Bearer ${secretKey}`;
    }

    const insert = await fetch(`${supabaseUrl}/rest/v1/game_scores`, {
      method: "POST",
      headers,
      body: JSON.stringify({
        nickname,
        score,
        kills,
        duration_ms: durationMs,
        app_version: appVersion || "1.0.0",
      }),
    });

    if (!insert.ok) {
      console.error("score_insert_failed", insert.status, await insert.text());
      return json({ error: "insert_failed" }, 500);
    }

    return json({ ok: true }, 201);
  } catch (error) {
    console.error("submit_score_error", error);
    return json({ error: "bad_request" }, 400);
  }
});
