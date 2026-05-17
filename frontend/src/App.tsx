import { useEffect, useMemo, useRef, useState } from "react";
import { Button, Container, Group, Select, Tabs, Title } from "@mantine/core";
import { useTranslation } from "react-i18next";
import {
  getGoals,
  getChats,
  getInterests,
  getIncomingRequests,
  getOutgoingRequests,
  getPostponedUsers,
  login,
  logout,
  me,
  register,
  Goal,
  Interest,
  UserProfile
} from "./api";
import AuthPanel from "./components/AuthPanel";
import RecommendationsPanel from "./components/RecommendationsPanel";
import RequestsPanel from "./components/RequestsPanel";
import ProfilePanel from "./components/ProfilePanel";
import PostponedPanel from "./components/PostponedPanel";
import ChatsPanel from "./components/ChatsPanel";

const languages = [
  { value: "ru", label: "Русский" },
  { value: "en", label: "English" }
];

const genders = [
  { value: "MALE", labelKey: "genderMale" },
  { value: "FEMALE", labelKey: "genderFemale" },
  { value: "OTHER", labelKey: "genderOther" }
] as const;

export default function App() {
  const { t, i18n } = useTranslation();
  const [ready, setReady] = useState(false);
  const [user, setUser] = useState<UserProfile | null>(null);
  const [interests, setInterests] = useState<Interest[]>([]);
  const [goals, setGoals] = useState<Goal[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [requestCount, setRequestCount] = useState(0);
  const [postponedCount, setPostponedCount] = useState(0);
  const [chatCount, setChatCount] = useState(0);
  const [syncTick, setSyncTick] = useState(0);
  const postponedGuardRef = useRef({
    value: 0,
    until: 0,
    mode: null as "min" | "max" | null
  });

  useEffect(() => {
    Promise.all([me(), getInterests(), getGoals()])
      .then(([meData, interestData, goalData]) => {
        setUser(meData);
        setInterests(interestData);
        setGoals(goalData);
      })
      .catch(async () => {
        setUser(null);
        try {
          const interestData = await getInterests();
          const goalData = await getGoals();
          setInterests(interestData);
          setGoals(goalData);
        } catch {
          // ignore
        }
      })
      .finally(() => setReady(true));
  }, []);

  const refreshCounters = async () => {
    if (!user) {
      setRequestCount(0);
      setPostponedCount(0);
      setChatCount(0);
      return;
    }
    const [incoming, outgoing, postponed, chats] = await Promise.all([
      getIncomingRequests(),
      getOutgoingRequests(),
      getPostponedUsers(),
      getChats()
    ]);
    setRequestCount(incoming.length + outgoing.length);
    setPostponedCount((prev) => {
      const guard = postponedGuardRef.current;
      if (Date.now() < guard.until && guard.mode === "max" && postponed.length < guard.value) {
        return Math.max(prev, guard.value);
      }
      if (Date.now() < guard.until && guard.mode === "min" && postponed.length > guard.value) {
        return Math.min(prev, guard.value);
      }
      postponedGuardRef.current = { value: postponed.length, until: 0, mode: null };
      return postponed.length;
    });
    setChatCount(chats.length);
  };

  const incrementPostponedCount = () => {
    setPostponedCount((prev) => {
      const next = prev + 1;
      postponedGuardRef.current = { value: next, until: Date.now() + 5000, mode: "max" };
      return next;
    });
    window.setTimeout(syncApp, 1000);
  };

  const decrementPostponedCount = () => {
    setPostponedCount((prev) => {
      const next = Math.max(0, prev - 1);
      postponedGuardRef.current = { value: next, until: Date.now() + 5000, mode: "min" };
      return next;
    });
    window.setTimeout(syncApp, 500);
  };

  const syncApp = async () => {
    await refreshCounters();
    setSyncTick(Date.now());
  };

  useEffect(() => {
    refreshCounters();
  }, [user?.id]);

  useEffect(() => {
    if (!user) {
      return;
    }
    const timer = window.setInterval(syncApp, 5000);
    return () => window.clearInterval(timer);
  }, [user?.id]);

  const interestOptions = useMemo(
    () =>
      interests.map((interest) => ({
        value: interest.code,
        label: i18n.language === "ru" ? interest.nameRu : interest.nameEn
      })),
    [interests, i18n.language]
  );

  const goalOptions = useMemo(
    () =>
      goals.map((goal) => ({
        value: goal.code,
        label: i18n.language === "ru" ? goal.nameRu : goal.nameEn
      })),
    [goals, i18n.language]
  );

  const genderOptions = useMemo(
    () =>
      genders.map((gender) => ({
        value: gender.value,
        label: t(gender.labelKey)
      })),
    [t]
  );

  if (!ready) {
    return <div className="app-shell">Loading...</div>;
  }

  return (
    <div className="app-shell">
      <Container size="xl">
        <Group justify="space-between" align="center" mb={12}>
          <Title order={2}>{t("appName")}</Title>
          <Group>
            <Select
              data={languages}
              value={i18n.language}
              onChange={(value) => value && i18n.changeLanguage(value)}
              allowDeselect={false}
              w={140}
            />
            {user && (
              <Button
                variant="light"
                onClick={async () => {
                  await logout();
                  setUser(null);
                }}
              >
                {t("logout")}
              </Button>
            )}
          </Group>
        </Group>

        {!user ? (
          <AuthPanel
            interestOptions={interestOptions}
            goalOptions={goalOptions}
            genderOptions={genderOptions}
            onLogin={async (email, password) => {
              setError(null);
              await login(email, password);
              const meData = await me();
              setUser(meData);
            }}
            onRegister={async (payload) => {
              setError(null);
              await register(payload);
              await login(payload.email, payload.password);
              const meData = await me();
              setUser(meData);
            }}
            error={error}
            setError={setError}
          />
        ) : (
          <Tabs defaultValue="recommendations">
            <Tabs.List mb={16}>
              <Tabs.Tab value="recommendations">{t("recommendations")}</Tabs.Tab>
              <Tabs.Tab value="requests">{t("requests")} {requestCount}</Tabs.Tab>
              <Tabs.Tab value="postponed">{t("postponedTab")} {postponedCount}</Tabs.Tab>
              <Tabs.Tab value="chats">{t("chats")} {chatCount}</Tabs.Tab>
              <Tabs.Tab value="profile">{t("profile")}</Tabs.Tab>
            </Tabs.List>

            <Tabs.Panel value="recommendations">
              <RecommendationsPanel
                user={user}
                interestOptions={interestOptions}
                goalOptions={goalOptions}
                genderOptions={genderOptions}
                onChanged={syncApp}
                onPostponed={incrementPostponedCount}
              />
            </Tabs.Panel>

            <Tabs.Panel value="requests">
              <RequestsPanel syncTick={syncTick} onChanged={syncApp} />
            </Tabs.Panel>

            <Tabs.Panel value="postponed">
              <PostponedPanel
                user={user}
                interestOptions={interestOptions}
                goalOptions={goalOptions}
                syncTick={syncTick}
                onChanged={syncApp}
                onPostponedRemoved={decrementPostponedCount}
              />
            </Tabs.Panel>

            <Tabs.Panel value="chats">
              <ChatsPanel user={user} syncTick={syncTick} onChanged={syncApp} />
            </Tabs.Panel>

            <Tabs.Panel value="profile">
              <ProfilePanel
                user={user}
                genderOptions={genderOptions}
                interestOptions={interestOptions}
                goalOptions={goalOptions}
                onUserChange={setUser}
              />
            </Tabs.Panel>
          </Tabs>
        )}
      </Container>
    </div>
  );
}
