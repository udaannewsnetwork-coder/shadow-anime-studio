const json = (data, status = 200) => new Response(JSON.stringify(data), { status, headers: { "content-type": "application/json; charset=utf-8", "cache-control": "no-store" } });
const err = (message, status = 400) => json({ ok: false, error: message }, status);
const MAX_AUDIO_BYTES = 18 * 1024 * 1024;
const MAX_JSON_BYTES = 256 * 1024;

function safeOrigin(request) {
  const origin = request.headers.get("Origin");
  if (!origin) return true;
  try {
    const u = new URL(origin);
    return u.hostname === "shadow-anime-studio.udaannewsnetwork.workers.dev";
  } catch { return false; }
}
async function ensureSchema(db) {
  await db.prepare(`CREATE TABLE IF NOT EXISTS projects (
    id TEXT PRIMARY KEY, workspace_key TEXT NOT NULL, title TEXT NOT NULL,
    data_json TEXT NOT NULL, created_at TEXT NOT NULL, updated_at TEXT NOT NULL
  )`).run();
  await db.prepare("CREATE INDEX IF NOT EXISTS idx_projects_workspace ON projects(workspace_key, updated_at)").run();
}
async function readJson(request) {
  const len = Number(request.headers.get("content-length") || 0);
  if (len > MAX_JSON_BYTES) throw new Error("Request is too large.");
  return request.json();
}
function getModelText(result) {
  if (typeof result === "string") return result;
  return result?.response || result?.result || result?.text || JSON.stringify(result);
}
export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname.startsWith("/api/")) {
      if (!safeOrigin(request)) return err("Origin not allowed.", 403);
      if (request.method === "OPTIONS") return new Response(null, {status:204,headers:{
        "Access-Control-Allow-Origin": url.origin, "Access-Control-Allow-Methods":"GET,POST,PUT,OPTIONS",
        "Access-Control-Allow-Headers":"Content-Type,X-Workspace-Key", "Access-Control-Max-Age":"86400"
      }});
      try {
        if (url.pathname === "/api/health" && request.method === "GET") {
          return json({ok:true,service:"SHADOW Anime Studio API",version:"0.1.0",
            engines:{text:!!env.AI,image:!!env.AI,speechToText:!!env.AI,textToSpeech:!!env.AI},
            storage:{database:!!env.DB,mediaBucket:!!env.MEDIA},
            models:{text:"@cf/meta/llama-3.2-3b-instruct",image:"@cf/black-forest-labs/flux-1-schnell",speechToText:"@cf/openai/whisper-large-v3-turbo",textToSpeech:"@cf/myshell-ai/melotts"},
            note:"Workers AI has a limited free daily allowance. Media cloud storage activates after R2 is enabled and bound."});
        }
        if (!env.AI) return err("AI engine binding is not configured yet.",503);
        if (url.pathname === "/api/story" && request.method === "POST") {
          const body = await readJson(request);
          const idea = String(body.idea || "").trim();
          if (!idea || idea.length > 5000) return err("Story idea must be 1–5000 characters.");
          const result = await env.AI.run("@cf/meta/llama-3.2-3b-instruct", {
            messages:[
              {role:"system",content:"You are SHADOW Anime Studio's anime story editor. Respond in the language requested. Create original, coherent anime concepts. Do not claim to generate video. Return clear headings, characters, and 5 scenes."},
              {role:"user",content:"Create an original anime episode package. Genre: "+String(body.genre||"Fantasy").slice(0,80)+". Language: "+String(body.language||"Hindi").slice(0,40)+". Idea: "+idea+"\nInclude title, logline, 3 characters with visual descriptions, and 5 scenes with action, dialogue, and image prompts."}
            ],
            max_tokens:1800,temperature:0.8
          });
          return json({ok:true,engine:"Cloudflare Workers AI",model:"@cf/meta/llama-3.2-3b-instruct",content:getModelText(result)});
        }
        if (url.pathname === "/api/image" && request.method === "POST") {
          const body = await readJson(request);
          const prompt = String(body.prompt||"").trim();
          if (!prompt || prompt.length > 2000) return err("Image prompt must be 1–2000 characters.");
          const result = await env.AI.run("@cf/black-forest-labs/flux-1-schnell",{prompt,steps:4,seed:Math.floor(Math.random()*2147483647)});
          if (!result?.image) return err("Image model returned no image.",502);
          return json({ok:true,model:"@cf/black-forest-labs/flux-1-schnell",mime:"image/jpeg",dataUrl:"data:image/jpeg;base64,"+result.image});
        }
        if (url.pathname === "/api/transcribe" && request.method === "POST") {
          const type = request.headers.get("content-type") || "";
          if (!type.includes("audio/") && !type.includes("application/octet-stream")) return err("Send an audio file as the raw request body with its audio content type.");
          const buffer = await request.arrayBuffer();
          if (!buffer.byteLength || buffer.byteLength > MAX_AUDIO_BYTES) return err("Audio must be between 1 byte and 18 MB.");
          const result = await env.AI.run("@cf/openai/whisper-large-v3-turbo",{audio:[...new Uint8Array(buffer)],task:"transcribe"});
          return json({ok:true,model:"@cf/openai/whisper-large-v3-turbo",text:result?.text||result?.transcription_info?.text||"",vtt:result?.vtt||"",segments:result?.segments||[]});
        }
        if (url.pathname === "/api/translate" && request.method === "POST") {
          const body = await readJson(request);
          const source = String(body.text||"").trim();
          if (!source || source.length > 6000) return err("Text must be 1–6000 characters.");
          const result = await env.AI.run("@cf/meta/llama-3.2-3b-instruct",{
            messages:[
              {role:"system",content:"You are a professional anime dubbing translator. Translate faithfully into natural spoken Hindi. Preserve character names, intent, emotion, and concise lip-sync-friendly phrasing. Return only the translated dialogue."},
              {role:"user",content:"Translate this dialogue into natural spoken Hindi:\n"+source}
            ],max_tokens:1200,temperature:0.25
          });
          return json({ok:true,model:"@cf/meta/llama-3.2-3b-instruct",translation:getModelText(result)});
        }
        if (url.pathname === "/api/tts" && request.method === "POST") {
          const body = await readJson(request);
          const text = String(body.text||"").trim();
          if (!text || text.length > 2500) return err("Dialogue must be 1–2500 characters.");
          const result = await env.AI.run("@cf/myshell-ai/melotts",{prompt:text,lang:String(body.lang||"hi").slice(0,8)});
          let bytes;
          if (result instanceof Uint8Array) bytes = result;
          else if (result instanceof ArrayBuffer) bytes = new Uint8Array(result);
          else if (Array.isArray(result?.audio)) bytes = new Uint8Array(result.audio);
          else if (typeof result?.audio === "string") bytes = Uint8Array.from(atob(result.audio),c=>c.charCodeAt(0));
          if (!bytes?.length) return err("TTS model returned an unsupported audio response.",502);
          return new Response(bytes,{headers:{"content-type":"audio/mpeg","cache-control":"no-store"}});
        }
        if (url.pathname === "/api/projects" && env.DB) {
          const key = request.headers.get("X-Workspace-Key") || "";
          if (!/^[a-f0-9]{32,80}$/i.test(key)) return err("Workspace key missing. Open this studio on the device where you created the workspace.",401);
          await ensureSchema(env.DB);
          if (request.method === "GET") {
            const rows = await env.DB.prepare("SELECT id,title,data_json,created_at,updated_at FROM projects WHERE workspace_key=? ORDER BY updated_at DESC LIMIT 50").bind(key).all();
            return json({ok:true,projects:(rows.results||[]).map(x=>({...x,data:JSON.parse(x.data_json)}))});
          }
          if (request.method === "POST" || request.method === "PUT") {
            const body = await readJson(request);
            const id = String(body.id||crypto.randomUUID()).slice(0,80);
            const title = String(body.title||"Untitled anime project").slice(0,160);
            const data = JSON.stringify(body.data||{});
            if (data.length > 180000) return err("Project data exceeds the 180 KB sync limit.");
            const now = new Date().toISOString();
            await env.DB.prepare(`INSERT INTO projects(id,workspace_key,title,data_json,created_at,updated_at)
              VALUES(?,?,?,?,?,?) ON CONFLICT(id) DO UPDATE SET title=excluded.title,data_json=excluded.data_json,updated_at=excluded.updated_at
              WHERE projects.workspace_key=excluded.workspace_key`).bind(id,key,title,data,now,now).run();
            return json({ok:true,id,title,updated_at:now});
          }
        }
        return err("API route not found.",404);
      } catch (e) {
        return err(e?.message || "Unexpected server error.",500);
      }
    }
    return env.ASSETS.fetch(request);
  }
};