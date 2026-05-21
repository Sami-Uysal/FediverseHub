# FediverseHub TODO

## MVP 1 Stabilization

- Add emulator smoke testing for platform switcher, bottom navigation and image loading.
- Add Compose previews for Mastodon, Lemmy and Pixelfed home content with mock data.
- Decide whether to keep AGP 9 compatibility flags or move to versions with native Hilt support.
- Re-enable Room compiler once the local Windows SQLite verifier temp path issue is resolved.

## Before Real API Integration

- Define DTO, entity, domain and UI model mapper packages per platform.
- Add account repository interface and mock implementation boundary.
- Add secure token storage plan: DataStore for MVP, Android Keystore backed storage before release.
- Implement dynamic base URL and auth token injection for Ktor requests.
- Add platform-specific API clients: MastodonApi, LemmyApi, PixelfedApi.
- Add paging contracts for Mastodon timeline, Lemmy feed and Pixelfed feed/grid.
- Add RemoteKey schema design scoped by accountId, platform, instanceUrl and feed type.
- Add user-facing error mapping for AppError in UI.
- Add baseline unit tests for platform switching and navigation destination state.
