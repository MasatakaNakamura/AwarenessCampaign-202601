INSERT INTO one_on_one (title, organizer, participant, start_at, end_at, location, status, tags, notes)
VALUES
  ('Initial Career Check-in', 'Manager A', 'Member A', NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day 1 hour', 'Video', 'SCHEDULED', 'career,goals', 'Discuss quarterly goals and blockers'),
  ('Weekly Sync', 'Manager B', 'Member B', NOW() + INTERVAL '2 days', NOW() + INTERVAL '2 days 1 hour', 'Office', 'SCHEDULED', 'planning', 'Review last week outcomes and next steps');
