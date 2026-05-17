import { useState } from "react";
import {
  Autocomplete,
  Button,
  Card,
  Divider,
  Group,
  MultiSelect,
  NumberInput,
  Select,
  Stack,
  Text,
  Textarea,
  TextInput,
  Title
} from "@mantine/core";
import { useTranslation } from "react-i18next";
import { updateMe, UserProfile } from "../api";
import { russianCities } from "../data/russianCities";

export type ProfilePanelProps = {
  user: UserProfile;
  genderOptions: { value: string; label: string }[];
  interestOptions: { value: string; label: string }[];
  goalOptions: { value: string; label: string }[];
  onUserChange: (user: UserProfile) => void;
};

const languages = [
  { value: "ru", label: "Русский" },
  { value: "en", label: "English" }
];

export default function ProfilePanel({
  user,
  genderOptions,
  interestOptions,
  goalOptions,
  onUserChange
}: ProfilePanelProps) {
  const { t } = useTranslation();
  const [editing, setEditing] = useState(false);
  const [status, setStatus] = useState<string | null>(null);
  const [form, setForm] = useState({
    firstName: user.firstName,
    lastName: user.lastName || "",
    age: user.age,
    city: user.city,
    language: user.language,
    gender: user.gender,
    about: user.about || "",
    interestCodes: user.interests,
    goalCodes: user.goals
  });
  const genderLabel =
    genderOptions.find((g) => g.value === user.gender)?.label ?? t("genderOther");
  const initials = `${user.firstName?.[0] || ""}${user.lastName?.[0] || ""}`.toUpperCase() || "U";

  return (
    <div className="panel">
      <Group justify="space-between" align="center" mb={12}>
        <Title order={4}>{t("profile")}</Title>
        <Group gap={8}>
          {status && <span className="status-pill">{status}</span>}
          <Button variant="light" onClick={() => setEditing((prev) => !prev)}>
            {editing ? t("cancel") : t("editProfile")}
          </Button>
        </Group>
      </Group>
      <Card className="card-shell" withBorder={false}>
        {!editing ? (
          <Stack gap="sm">
            <div className="profile-hero">
              <div className="avatar avatar--profile avatar--photo">
                <span className="avatar-initials">{initials}</span>
              </div>
              <div>
                <Title order={3}>
                  {user.firstName} {user.lastName}
                </Title>
                <Text className="helper-text">{user.city}</Text>
              </div>
            </div>
            <Group grow>
              <div>
                <Text size="sm" c="dimmed">{t("firstName")}</Text>
                <Text fw={600}>{user.firstName}</Text>
              </div>
              <div>
                <Text size="sm" c="dimmed">{t("lastName")}</Text>
                <Text fw={600}>{user.lastName || "—"}</Text>
              </div>
            </Group>
            <Group grow>
              <div>
                <Text size="sm" c="dimmed">{t("age")}</Text>
                <Text fw={600}>{user.age} лет</Text>
              </div>
              <div>
                <Text size="sm" c="dimmed">{t("gender")}</Text>
                <Text fw={600}>{genderLabel}</Text>
              </div>
            </Group>
            <Group grow>
              <div>
                <Text size="sm" c="dimmed">{t("city")}</Text>
                <Text fw={600}>{user.city}</Text>
              </div>
              <div>
                <Text size="sm" c="dimmed">{t("language")}</Text>
                <Text fw={600}>{user.language}</Text>
              </div>
            </Group>
            <Divider />
            <div>
              <Text size="sm" c="dimmed">{t("about")}</Text>
              <Text>{user.about || "—"}</Text>
            </div>
          </Stack>
        ) : (
          <Stack gap="sm">
            <Group grow>
              <TextInput
                label={t("firstName")}
                value={form.firstName}
                onChange={(event) => setForm((prev) => ({ ...prev, firstName: event.currentTarget.value }))}
              />
              <TextInput
                label={t("lastName")}
                value={form.lastName}
                onChange={(event) => setForm((prev) => ({ ...prev, lastName: event.currentTarget.value }))}
              />
            </Group>
            <Group grow>
              <NumberInput
                label={t("age")}
                min={16}
                max={120}
                value={form.age}
                onChange={(value) => setForm((prev) => ({ ...prev, age: Number(value || 0) }))}
              />
              <Autocomplete
                label={t("city")}
                data={russianCities}
                limit={8}
                value={form.city}
                onChange={(value) => setForm((prev) => ({ ...prev, city: value }))}
              />
            </Group>
            <Group grow>
              <Select
                label={t("language")}
                data={languages}
                value={form.language}
                onChange={(value) => setForm((prev) => ({ ...prev, language: value || "ru" }))}
              />
              <Select
                label={t("gender")}
                data={genderOptions}
                value={form.gender}
                onChange={(value) => setForm((prev) => ({ ...prev, gender: (value || "OTHER") as UserProfile["gender"] }))}
              />
            </Group>
            <Textarea
              label={t("about")}
              minRows={3}
              value={form.about}
              onChange={(event) => setForm((prev) => ({ ...prev, about: event.currentTarget.value }))}
            />
            <MultiSelect
              label={t("interests")}
              data={interestOptions}
              value={form.interestCodes}
              onChange={(value) => setForm((prev) => ({ ...prev, interestCodes: value }))}
              searchable
              maxDropdownHeight={260}
              nothingFoundMessage={t("nothingFound")}
              description={`${t("selected")}: ${form.interestCodes.length}`}
            />
            <MultiSelect
              label={t("goals")}
              data={goalOptions}
              value={form.goalCodes}
              onChange={(value) => setForm((prev) => ({ ...prev, goalCodes: value }))}
              searchable
              maxDropdownHeight={260}
              nothingFoundMessage={t("nothingFound")}
              description={`${t("selected")}: ${form.goalCodes.length}`}
            />
            <Button
              onClick={async () => {
                const updated = await updateMe(form);
                onUserChange(updated);
                setStatus(t("profileSaved"));
                setEditing(false);
              }}
            >
              {t("save")}
            </Button>
          </Stack>
        )}
      </Card>
    </div>
  );
}
