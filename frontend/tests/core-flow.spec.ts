import { expect, test } from '@playwright/test';

test('login, project, task, report worker, and UI status complete end to end', async ({ page }) => {
  test.setTimeout(90_000);
  const projectName = `E2E Project ${Date.now()}`;

  await page.goto('/login');
  await page.getByPlaceholder('Enter your username').fill('manager');
  await page.locator('input[type="password"]').fill('manager123!');
  await page.getByRole('button', { name: 'Sign In' }).click();
  await expect(page).toHaveURL(/\/dashboard$/);

  const created = await page.evaluate(async ({ name }) => {
    const token = localStorage.getItem('token');
    const headers = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };
    const projectResponse = await fetch('http://localhost:8080/api/projects', {
      method: 'POST',
      headers,
      body: JSON.stringify({ name, description: 'Created by the full-stack Playwright test' }),
    });
    if (!projectResponse.ok) throw new Error(`Project creation failed: ${projectResponse.status}`);
    const project = await projectResponse.json();
    const taskResponse = await fetch(`http://localhost:8080/api/projects/${project.id}/tasks`, {
      method: 'POST',
      headers,
      body: JSON.stringify({ title: 'End-to-end task', description: 'Worker input', priority: 'HIGH' }),
    });
    if (!taskResponse.ok) throw new Error(`Task creation failed: ${taskResponse.status}`);
    const task = await taskResponse.json();
    return { projectId: project.id as number, taskId: task.id as number };
  }, { name: projectName });

  await page.goto('/reports');
  await expect(page.getByText('Live')).toBeVisible({ timeout: 15_000 });
  await page.getByRole('button', { name: 'Generate Report' }).click();
  await page.locator('select').first().selectOption({ label: projectName });

  const reportRequest = page.waitForResponse(response =>
    response.url().endsWith('/api/reports') && response.request().method() === 'POST'
  );
  await page.getByRole('button', { name: 'Generate', exact: true }).click();
  const reportResponse = await reportRequest;
  expect(reportResponse.ok()).toBeTruthy();
  const queued = await reportResponse.json() as { reportId: number; jobId: number; status: string };
  expect(queued.status).toBe('QUEUED');

  const reportCard = page.getByText(`Project #${created.projectId}`).locator('..').locator('..');
  await expect(reportCard.getByText('COMPLETED')).toBeVisible({ timeout: 30_000 });
  await page.getByRole('button', { name: 'Refresh' }).click();
  await expect(reportCard.getByRole('button', { name: 'View' })).toBeVisible();

  const completedJob = await page.evaluate(async (jobId) => {
    const token = localStorage.getItem('token');
    const response = await fetch(`http://localhost:8080/api/jobs/${jobId}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.json();
  }, queued.jobId);
  expect(completedJob.status).toBe('COMPLETED');
});
