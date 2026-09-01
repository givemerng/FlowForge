import { test, expect } from '@playwright/test';

test('Core Flow: Login and View Dashboard', async ({ page }) => {
  // 1. Navigate to root
  await page.goto('/');

  // Should redirect to login
  await expect(page).toHaveURL(/.*\/login/);

  // 2. Fill login form (assuming basic auth form structure based on standard React patterns)
  // These selectors will likely need to be adjusted based on the actual DOM.
  // Assuming inputs have names or placeholders
  await page.fill('input[type="text"], input[name="username"], input[placeholder*="username" i]', 'testuser');
  await page.fill('input[type="password"]', 'password123');
  
  await page.click('button[type="submit"]');

  // 3. Verify successful login redirects to dashboard or projects
  await expect(page).toHaveURL(/.*\/projects|.*\/dashboard/);
  
  // 4. Verify some element on the dashboard/projects page
  // We expect a header or a new project button
  const newProjectButton = page.locator('button', { hasText: /New Project/i });
  await expect(newProjectButton).toBeVisible({ timeout: 10000 });
});
