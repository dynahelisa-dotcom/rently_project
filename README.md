# Rently · Landlord-Tenant Management System (Milestone 1)

Kotlin · Jetpack Compose (Material 3) · Supabase (Auth + Postgres) · min SDK 26

## Run it
1. Open the `Rently` folder in Android Studio (Ladybug or newer) and let Gradle sync.
2. **Demo mode:** just press Run. With no Supabase keys the app uses sample data (any email + 6+ char password).
3. **Real backend:**
   - Create a Supabase project and run `supabase/schema.sql` in the SQL Editor (sample-data block at the bottom).
   - Add to `local.properties`:
     ```
     SUPABASE_URL=https://YOUR-PROJECT.supabase.co
     SUPABASE_ANON_KEY=YOUR-ANON-KEY
     ```
   - Sync and run, then sign in with the landlord user you created.

## Screen flow (the one full landlord feature: rent tracking)
```
Login ──► Dashboard ──► Tenants (All/Paid/Owing/Unpaid + search) ──► Tenant detail
                │                                                      └─ Record payment → receipt
                └─ "Needs attention" rows jump straight to Tenant detail
```
Recording a payment inserts a row in `payments`; the `tenant_rent_status` view recalculates paid / balance / status, so the dashboard, the list and the detail screen all update.

## Module map
| # | Module | Covers (from your document) | Status |
|---|--------|-----------------------------|--------|
| 1 | Auth & profiles | register, login, reset password, landlord/tenant roles | Login done |
| 2 | Property & rooms | add/edit/delete rooms, compound profile | Schema only |
| 3 | Booking | tenant books, landlord approves/rejects, notify | Next |
| 4 | Rent & payments | set rent/due date, record payment, receipt, balance, PayChangu | Record + receipt done |
| 5 | Dashboard & reports | paid/unpaid/owing counts, chart, reports | Dashboard done, reports next |
| 6 | Chat | tenant ↔ landlord messages, history | Next (Supabase Realtime) |
| 7 | Issues | report, category/urgency, In Progress / Fixed | Next |
| 8 | Notifications | reminders, booking updates | Last (FCM + scheduled job) |

## Code layout
```
data/        Models, RentRepository (interface), SupabaseRentRepository, DemoRentRepository
ui/theme/    One indigo accent + neutrals
ui/components/  AppCard (shadow), StatusBadge, DonutChart, fields, buttons
ui/auth/ ui/dashboard/ ui/tenants/   the four screens
ui/AppNav.kt, AuthViewModel.kt, RentViewModel.kt
```
