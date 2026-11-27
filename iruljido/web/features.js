// Preserve existing friend data while the in-app friend screens are unavailable.
export const inAppFriendsEnabled = false;

const friendPages = new Set(['friends', 'boards', 'board', 'nearCard']);

export function pageEnabled(name) {
  return inAppFriendsEnabled || !friendPages.has(name);
}
