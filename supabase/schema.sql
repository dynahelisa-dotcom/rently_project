-- Rently · Supabase schema (run in SQL Editor)
-- Milestone 1 uses: profiles, properties, rooms, tenancies, payments, tenant_rent_status.

create table profiles (
  id uuid primary key references auth.users on delete cascade,
  full_name text not null,
  phone text,
  role text not null check (role in ('landlord', 'tenant'))
);

create table properties (
  id uuid primary key default gen_random_uuid(),
  landlord_id uuid not null references profiles(id) on delete cascade,
  name text not null,
  address text
);

create table rooms (
  id uuid primary key default gen_random_uuid(),
  property_id uuid not null references properties(id) on delete cascade,
  label text not null,
  monthly_rent numeric not null check (monthly_rent >= 0),
  status text not null default 'available' check (status in ('available', 'occupied'))
);

-- tenant_id is optional: a landlord can add a tenant who has not installed the app yet.
create table tenancies (
  id uuid primary key default gen_random_uuid(),
  room_id uuid not null references rooms(id) on delete cascade,
  tenant_id uuid references profiles(id),
  tenant_name text not null,
  phone text,
  monthly_rent numeric not null check (monthly_rent >= 0),
  due_day int not null default 1 check (due_day between 1 and 28),
  active boolean not null default true
);

create table payments (
  id uuid primary key default gen_random_uuid(),
  tenancy_id uuid not null references tenancies(id) on delete cascade,
  amount numeric not null check (amount > 0),
  method text not null,
  reference text not null default ('RCP-' || upper(substr(md5(gen_random_uuid()::text), 1, 8))),
  paid_at timestamptz not null default now()
);

-- Paid / owing / unpaid is derived from this view (current calendar month).
create view tenant_rent_status with (security_invoker = true) as
select
  t.id as tenancy_id,
  pr.landlord_id,
  t.tenant_name,
  coalesce(t.phone, '') as phone,
  r.label as room_label,
  t.monthly_rent,
  coalesce(sum(pay.amount) filter (where pay.paid_at >= date_trunc('month', now())), 0) as paid_amount,
  t.due_day
from tenancies t
join rooms r on r.id = t.room_id
join properties pr on pr.id = r.property_id
left join payments pay on pay.tenancy_id = t.id
where t.active
group by t.id, pr.landlord_id, t.tenant_name, t.phone, r.label, t.monthly_rent, t.due_day;

-- Row Level Security: users only see their own data.
alter table profiles enable row level security;
alter table properties enable row level security;
alter table rooms enable row level security;
alter table tenancies enable row level security;
alter table payments enable row level security;

create policy "own profile" on profiles for select using (id = auth.uid());

create policy "landlord owns properties" on properties for all
  using (landlord_id = auth.uid()) with check (landlord_id = auth.uid());

create policy "landlord owns rooms" on rooms for all
  using (exists (select 1 from properties p where p.id = rooms.property_id and p.landlord_id = auth.uid()))
  with check (exists (select 1 from properties p where p.id = rooms.property_id and p.landlord_id = auth.uid()));

create policy "landlord owns tenancies" on tenancies for all
  using (exists (select 1 from rooms r join properties p on p.id = r.property_id
                 where r.id = tenancies.room_id and p.landlord_id = auth.uid()))
  with check (exists (select 1 from rooms r join properties p on p.id = r.property_id
                      where r.id = tenancies.room_id and p.landlord_id = auth.uid()));
create policy "tenant reads own tenancy" on tenancies for select using (tenant_id = auth.uid());

create policy "landlord reads payments" on payments for select
  using (exists (select 1 from tenancies t join rooms r on r.id = t.room_id join properties p on p.id = r.property_id
                 where t.id = payments.tenancy_id and p.landlord_id = auth.uid()));
create policy "landlord records payments" on payments for insert
  with check (exists (select 1 from tenancies t join rooms r on r.id = t.room_id join properties p on p.id = r.property_id
                      where t.id = payments.tenancy_id and p.landlord_id = auth.uid()));
create policy "tenant reads own payments" on payments for select
  using (exists (select 1 from tenancies t where t.id = payments.tenancy_id and t.tenant_id = auth.uid()));

-- ---------------------------------------------------------------
-- Optional sample data
-- 1) Authentication > Users > Add user (your landlord email + password) and copy its UUID.
-- 2) Replace <LANDLORD_UUID> below and run.
-- ---------------------------------------------------------------
-- insert into profiles (id, full_name, role) values ('<LANDLORD_UUID>', 'Your Name', 'landlord');
--
-- with p as (
--   insert into properties (landlord_id, name) values ('<LANDLORD_UUID>', 'Sunrise Court') returning id
-- ), r as (
--   insert into rooms (property_id, label, monthly_rent, status)
--   select p.id, v.label, v.rent, 'occupied' from p,
--     (values ('A1', 150000), ('A2', 120000), ('B1', 180000)) as v(label, rent)
--   returning id, label, monthly_rent
-- )
-- insert into tenancies (room_id, tenant_name, phone, monthly_rent, due_day)
-- select r.id,
--   case r.label when 'A1' then 'Chikondi Banda' when 'A2' then 'Thoko Phiri' else 'Yamikani Mwale' end,
--   '0999 000 000', r.monthly_rent, 5
-- from r;
--
-- insert into payments (tenancy_id, amount, method)
-- select id, 150000, 'Cash' from tenancies where tenant_name = 'Chikondi Banda';
