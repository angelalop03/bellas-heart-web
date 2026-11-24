// Aplica las migraciones de db/migrations que falten (en orden y una sola vez).
// Se ejecuta en cada despliegue de Vercel antes de compilar la web, así la base de datos de Neon
// queda creada y actualizada sin tener que copiar nada a mano.
import { readdir, readFile } from 'node:fs/promises';
import { existsSync, readFileSync } from 'node:fs';
import path from 'node:path';
import pg from 'pg';

function loadLocalEnv() {
  // En local, lee DATABASE_URL de .env.local (en Vercel ya viene en el entorno)
  for (const file of ['.env.local', '.env']) {
    if (!existsSync(file)) continue;
    for (const line of readFileSync(file, 'utf8').split(/\r?\n/)) {
      const m = line.match(/^\s*([A-Z0-9_]+)\s*=\s*(.*)\s*$/);
      if (m && !process.env[m[1]]) process.env[m[1]] = m[2].replace(/^["']|["']$/g, '');
    }
  }
}

loadLocalEnv();
const url = process.env.DATABASE_URL || process.env.POSTGRES_URL;
if (!url) {
  console.warn('[migrate] No hay DATABASE_URL: se omiten las migraciones.');
  process.exit(0);
}

const client = new pg.Client({
  connectionString: url,
  ssl: /localhost|127\.0\.0\.1/.test(url) ? false : { rejectUnauthorized: false },
});

const dir = path.resolve('db/migrations');
await client.connect();
try {
  // Evita que dos despliegues migren a la vez
  await client.query('select pg_advisory_lock(424242)');
  await client.query(`create table if not exists schema_migrations (
    name text primary key,
    applied_at timestamptz not null default now()
  )`);
  const { rows } = await client.query('select name from schema_migrations');
  const done = new Set(rows.map((r) => r.name));
  const files = (await readdir(dir)).filter((f) => f.endsWith('.sql')).sort();
  let applied = 0;
  for (const file of files) {
    if (done.has(file)) continue;
    const sql = await readFile(path.join(dir, file), 'utf8');
    process.stdout.write(`[migrate] Aplicando ${file}… `);
    await client.query('begin');
    try {
      await client.query(sql);
      await client.query('insert into schema_migrations (name) values ($1)', [file]);
      await client.query('commit');
      console.log('ok');
      applied++;
    } catch (error) {
      await client.query('rollback');
      console.log('ERROR');
      throw error;
    }
  }
  console.log(applied ? `[migrate] ${applied} migración(es) aplicada(s).` : '[migrate] La base de datos ya estaba al día.');
} finally {
  await client.query('select pg_advisory_unlock(424242)').catch(() => undefined);
  await client.end();
}
