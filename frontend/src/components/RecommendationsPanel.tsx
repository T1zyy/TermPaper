import { useEffect, useState } from "react";
import {
  Button,
  Card,
  Group,
  Autocomplete,
  RangeSlider,
  Select,
  Stack,
  Text,
  Textarea,
  Title,
  MultiSelect
} from "@mantine/core";
import { useTranslation } from "react-i18next";
import {
  getRecommendations,
  postponeUser,
  recordView,
  sendRequest,
  Recommendation,
  UserProfile
} from "../api";
import { russianCities } from "../data/russianCities";
import ToastNotice from "./ToastNotice";

const languages = [
  { value: "ru", label: "Русский" },
  { value: "en", label: "English" }
];

export type RecommendationsPanelProps = {
  user: UserProfile;
  interestOptions: { value: string; label: string }[];
  goalOptions: { value: string; label: string }[];
  genderOptions: { value: string; label: string }[];
  onChanged?: () => void;
  onPostponed?: () => void;
};

export default function RecommendationsPanel({
  user,
  interestOptions,
  goalOptions,
  genderOptions,
  onChanged,
  onPostponed
}: RecommendationsPanelProps) {
  const { t } = useTranslation();
  const [filter, setFilter] = useState({
    ageFrom: 16 as number | undefined,
    ageTo: 35 as number | undefined,
    city: user.city,
    language: user.language,
    gender: undefined as "MALE" | "FEMALE" | "OTHER" | undefined,
    goals: [] as string[],
    interests: [] as string[],
    commonOnly: false,
    q: ""
  });
  const [ageRange, setAgeRange] = useState<[number, number]>([16, 35]);
  const [items, setItems] = useState<Recommendation[]>([]);
  const [index, setIndex] = useState(0);
  const [message, setMessage] = useState("");
  const [toast, setToast] = useState<string | null>(null);

  const load = async () => {
    const data = await getRecommendations(filter);
    setItems(data);
    setIndex(0);
  };

  useEffect(() => {
    load();
  }, []);

  const current = items[index];
  const hasPrev = index > 0;
  const total = items.length;
  const currentPosition = current ? index + 1 : Math.min(index + 1, total);

  const commonInterests = current
    ? current.interests.filter((interest) => user.interests.includes(interest))
    : [];
  const otherInterests = current
    ? current.interests.filter((interest) => !user.interests.includes(interest))
    : [];
  const commonInterestCount = commonInterests.length;
  const nearAgeRange: [number, number] = [
    Math.max(16, user.age - 5),
    Math.min(120, user.age + 5)
  ];
  const isMyCityActive = filter.city.trim().toLowerCase() === user.city.trim().toLowerCase();
  const isNearAgeActive = ageRange[0] === nearAgeRange[0] && ageRange[1] === nearAgeRange[1];

  const resetFilters = () => {
    setAgeRange([16, 35]);
    setFilter({
      ageFrom: 16,
      ageTo: 35,
      city: "",
      language: user.language,
      gender: undefined,
      goals: [],
      interests: [],
      commonOnly: false,
      q: ""
    });
  };

  const applyMyCity = () => {
    setFilter((prev) => ({ ...prev, city: isMyCityActive ? "" : user.city }));
  };

  const applyNearAge = () => {
    const nextRange: [number, number] = isNearAgeActive ? [16, 35] : nearAgeRange;
    setAgeRange(nextRange);
    setFilter((prev) => ({ ...prev, ageFrom: nextRange[0], ageTo: nextRange[1] }));
  };

  const applyCommonOnly = () => {
    setFilter((prev) => ({ ...prev, commonOnly: !prev.commonOnly }));
  };

  const goBack = () => {
    setIndex((prev) => Math.max(prev - 1, 0));
  };

  const goNext = async () => {
    if (!current) {
      return;
    }
    await recordView(current.id);
    setIndex((prev) => Math.min(prev + 1, items.length));
    setMessage("");
  };

  return (
    <div className="split">
      <ToastNotice message={toast} onClose={() => setToast(null)} />
      <div className="panel">
        <Title order={4} mb={12}>
          {t("filters")}
        </Title>
          <Stack gap="sm">
          <div>
            <Text size="sm" fw={600} mb={6}>
              {t("quickFilters")}
            </Text>
            <Group gap={8}>
              <Button
                className={isMyCityActive ? "toggle-button toggle-button--active" : "toggle-button"}
                variant="light"
                size="xs"
                onClick={applyMyCity}
              >
                {t("myCity")}
              </Button>
              <Button
                className={isNearAgeActive ? "toggle-button toggle-button--active" : "toggle-button"}
                variant="light"
                size="xs"
                onClick={applyNearAge}
              >
                {t("ageNearMe")}
              </Button>
              <Button
                className={filter.commonOnly ? "toggle-button toggle-button--active" : "toggle-button"}
                variant="light"
                size="xs"
                onClick={applyCommonOnly}
              >
                {t("hasCommonInterests")}
              </Button>
            </Group>
          </div>
          <Stack gap={6}>
            <Text size="sm" fw={600}>
              {t("ageRange")}
            </Text>
            <RangeSlider
              min={16}
              max={120}
              step={1}
              value={ageRange}
              onChange={(value) => {
                setAgeRange(value);
                setFilter((prev) => ({ ...prev, ageFrom: value[0], ageTo: value[1] }));
              }}
              label={(value) => `${value}`}
            />
            <Text size="sm" c="dimmed">
              {ageRange[0]} – {ageRange[1]} {t("age")}
            </Text>
          </Stack>
          <Autocomplete
            label={t("search")}
            value={filter.q}
            data={[]}
            onChange={(value) => setFilter((prev) => ({ ...prev, q: value }))}
          />
          <Autocomplete
            label={t("city")}
            data={russianCities}
            limit={8}
            value={filter.city}
            onChange={(value) => setFilter((prev) => ({ ...prev, city: value }))}
          />
          <Select
            label={t("language")}
            data={languages}
            value={filter.language}
            onChange={(value) =>
              setFilter((prev) => ({ ...prev, language: value || "ru" }))
            }
          />
          <Select
            label={t("gender")}
            data={genderOptions}
            value={filter.gender}
            onChange={(value) =>
              setFilter((prev) => ({ ...prev, gender: value as "MALE" | "FEMALE" | "OTHER" | undefined }))
            }
            clearable
          />
          <MultiSelect
            label={t("goals")}
            data={goalOptions}
            value={filter.goals}
            onChange={(value) => setFilter((prev) => ({ ...prev, goals: value }))}
            searchable
            maxDropdownHeight={260}
            nothingFoundMessage={t("nothingFound")}
            description={`${t("selected")}: ${filter.goals.length}`}
          />
          <MultiSelect
            label={t("interests")}
            data={interestOptions}
            value={filter.interests}
            onChange={(value) => setFilter((prev) => ({ ...prev, interests: value }))}
            searchable
            maxDropdownHeight={260}
            nothingFoundMessage={t("nothingFound")}
            description={`${t("selected")}: ${filter.interests.length}`}
          />
          <Group grow>
            <Button onClick={load}>{t("recommendations")}</Button>
            <Button variant="light" onClick={resetFilters}>{t("reset")}</Button>
          </Group>
        </Stack>
      </div>

      <div className="panel">
        <Group justify="space-between" align="center" mb={16}>
          <Title order={4}>{t("recommendations")}</Title>
          <Group gap={8}>
            {current && (
              <span className="status-pill status-pill--neutral">
                {currentPosition}/{total}
              </span>
            )}
          </Group>
        </Group>

        {!current ? (
          <Card className="card-shell" radius="lg" withBorder={false}>
            <Stack gap="sm">
              <Text className="helper-text">{t("noRecommendations")}</Text>
              {hasPrev && (
                <Button
                  variant="light"
                  onClick={goBack}
                >
                  {t("back")}
                </Button>
              )}
            </Stack>
          </Card>
        ) : (
          <Card className="card-shell recommendation-card" radius="lg" withBorder={false}>
            <button
              className="card-arrow card-arrow--left"
              type="button"
              onClick={goBack}
              disabled={!hasPrev}
              aria-label={t("back")}
            >
              ‹
            </button>
            <button
              className="card-arrow card-arrow--right"
              type="button"
              onClick={goNext}
              disabled={index >= items.length - 1}
              aria-label={t("next")}
            >
              ›
            </button>
            <div className="card-header">
              <div className="avatar">
                {(current.firstName?.[0] || "").toUpperCase()}
                {(current.lastName?.[0] || "").toUpperCase()}
              </div>
              <div className="card-title">
                <Title order={3}>
                  {current.firstName} {current.lastName}
                </Title>
                <div className="meta-row">
                  <span className="meta-item">
                    <span className="meta-dot" /> {current.age} лет
                  </span>
                  <span className="meta-item">
                    <span className="meta-dot" /> {current.city}
                  </span>
                  <span className="meta-item">
                    <span className="meta-dot" /> {current.language}
                  </span>
                </div>
              </div>
              <div className="tag-grid">
                <span className="tag tag--match-count">
                  {commonInterestCount} {t("commonInterestsShort")}
                </span>
                <span className="tag">
                  {t("gender")} ·{" "}
                  {t(
                    current.gender === "MALE"
                      ? "genderMale"
                      : current.gender === "FEMALE"
                      ? "genderFemale"
                      : "genderOther"
                  )}
                </span>
              </div>
            </div>

            <Stack mt={12} gap="sm">
              <div>
                <Text fw={600} mb={6}>
                  {t("commonInterests")} · {commonInterestCount}
                </Text>
                <Group gap={8}>
                  {commonInterests.length === 0 && (
                    <Text size="sm" c="dimmed">
                      —
                    </Text>
                  )}
                  {commonInterests.map((interest) => (
                    <span key={interest} className="badge badge--match">
                      {interestOptions.find((item) => item.value === interest)?.label || interest}
                    </span>
                  ))}
                </Group>
              </div>

              {current.about && (
                <div>
                  <Text fw={600} mb={4}>
                    {t("about")}
                  </Text>
                  <Text>{current.about}</Text>
                </div>
              )}

              {otherInterests.length > 0 && (
                <div>
                  <Text fw={600} mb={6}>
                    {t("otherInterests")}
                  </Text>
                  <Group gap={8}>
                    {otherInterests.map((interest) => (
                      <span key={interest} className="badge badge--muted">
                        {interestOptions.find((item) => item.value === interest)?.label || interest}
                      </span>
                    ))}
                  </Group>
                </div>
              )}

              {current.goals.length > 0 && (
                <div>
                  <Text fw={600} mb={6}>
                    {t("goals")}
                  </Text>
                  <Group gap={8}>
                    {current.goals.map((goal) => (
                      <span key={goal} className="badge">
                        {goalOptions.find((item) => item.value === goal)?.label || goal}
                      </span>
                    ))}
                  </Group>
                </div>
              )}

              <Textarea
                label={t("message")}
                minRows={1}
                value={message}
                onChange={(event) => setMessage(event.currentTarget.value)}
              />

              <Group>
                <Button
                  onClick={async () => {
                    await sendRequest(current.id, message || undefined);
                    setToast(t("requestSent"));
                    setItems((prev) => prev.filter((item) => item.id !== current.id));
                    setIndex((prev) => Math.min(prev, Math.max(0, items.length - 2)));
                    setMessage("");
                    onChanged?.();
                  }}
                >
                  {t("request")}
                </Button>
                <Button
                  variant="outline"
                  onClick={async () => {
                    await postponeUser(current.id);
                    setItems((prev) => prev.filter((item) => item.id !== current.id));
                    setIndex((prev) => Math.min(prev, Math.max(0, items.length - 2)));
                    setMessage("");
                    setToast(t("postponed"));
                    onPostponed?.();
                  }}
                >
                  {t("postpone")}
                </Button>
              </Group>
            </Stack>
          </Card>
        )}
      </div>
    </div>
  );
}
