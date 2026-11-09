create table public.game_scores (
  id uuid primary key default gen_random_uuid(),
  nickname varchar(14) not null check (char_length(trim(nickname)) between 1 and 14),
  score integer not null check (score between 0 and 1000000),
  kills integer not null default 0 check (kills between 0 and 10000),
  duration_ms integer not null check (duration_ms between 1000 and 21600000),
  app_version text not null default '1.0.0' check (char_length(app_version) between 1 and 32),
  created_at timestamptz not null default now()
);

alter table public.game_scores enable row level security;

revoke all on table public.game_scores from anon, authenticated;
grant select on table public.game_scores to anon, authenticated;
grant select, insert on table public.game_scores to service_role;

create policy "public leaderboard is readable"
on public.game_scores
for select
to anon, authenticated
using (true);

create index game_scores_score_created_idx
on public.game_scores (score desc, created_at asc);
