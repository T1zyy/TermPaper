import { useState } from "react";
import {
  Button,
  Group,
  Autocomplete,
  NumberInput,
  PasswordInput,
  Select,
  Stack,
  Tabs,
  Text,
  TextInput,
  Textarea,
  Title,
  MultiSelect
} from "@mantine/core";
import { useTranslation } from "react-i18next";
import { russianCities } from "../data/russianCities";

export type AuthPanelProps = {
  interestOptions: { value: string; label: string }[];
  goalOptions: { value: string; label: string }[];
  genderOptions: { value: string; label: string }[];
  onLogin: (email: string, password: string) => Promise<void>;
  onRegister: (payload: {
    email: string;
    password: string;
    firstName: string;
    lastName?: string;
    age: number;
    city: string;
    language: string;
    gender: "MALE" | "FEMALE" | "OTHER";
    about?: string;
    interestCodes: string[];
    goalCodes?: string[];
  }) => Promise<void>;
  error: string | null;
  setError: (value: string | null) => void;
};

export default function AuthPanel({
  interestOptions,
  goalOptions,
  genderOptions,
  onLogin,
  onRegister,
  error,
  setError
}: AuthPanelProps) {
  const { t } = useTranslation();
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");

  const [form, setForm] = useState({
    email: "",
    password: "",
    firstName: "",
    lastName: "",
    age: 16,
    city: "",
    language: "ru",
    gender: "MALE" as "MALE" | "FEMALE" | "OTHER",
    about: "",
    interestCodes: [] as string[],
    goalCodes: [] as string[]
  });

  return (
    <div className="split">
      <div className="hero">
        <Title order={1} mb={12}>
          {t("appName")}
        </Title>
        <Text className="helper-text" c="gray.2">
          Подбирай собеседников по интересам и находи тех, с кем действительно
          есть о чем говорить.
        </Text>
      </div>

      <div className="panel">
        <Tabs defaultValue="login">
          <Tabs.List>
            <Tabs.Tab value="login">{t("loginTitle")}</Tabs.Tab>
            <Tabs.Tab value="register">{t("registerTitle")}</Tabs.Tab>
          </Tabs.List>

          {error && (
            <Text c="red" mt={12}>
              {error}
            </Text>
          )}

          <Tabs.Panel value="login" pt={20}>
            <Stack>
              <TextInput
                label={t("email")}
                value={loginEmail}
                onChange={(event) => setLoginEmail(event.currentTarget.value)}
              />
              <PasswordInput
                label={t("password")}
                value={loginPassword}
                onChange={(event) => setLoginPassword(event.currentTarget.value)}
              />
              <Button
                onClick={async () => {
                  try {
                    await onLogin(loginEmail, loginPassword);
                  } catch (e) {
                    setError(e instanceof Error ? e.message : "Ошибка");
                  }
                }}
              >
                {t("login")}
              </Button>
            </Stack>
          </Tabs.Panel>

          <Tabs.Panel value="register" pt={20}>
            <Stack>
              <Group grow>
                <TextInput
                  label={t("email")}
                  value={form.email}
                  onChange={(event) =>
                    setForm((prev) => ({ ...prev, email: event.currentTarget.value }))
                  }
                />
                <PasswordInput
                  label={t("password")}
                  value={form.password}
                  onChange={(event) =>
                    setForm((prev) => ({ ...prev, password: event.currentTarget.value }))
                  }
                />
              </Group>
              <Group grow>
                <TextInput
                  label={t("firstName")}
                  value={form.firstName}
                  onChange={(event) =>
                    setForm((prev) => ({ ...prev, firstName: event.currentTarget.value }))
                  }
                />
                <TextInput
                  label={t("lastName")}
                  value={form.lastName}
                  onChange={(event) =>
                    setForm((prev) => ({ ...prev, lastName: event.currentTarget.value }))
                  }
                />
              </Group>
              <Group grow>
                <NumberInput
                  label={t("age")}
                  min={16}
                  max={120}
                  value={form.age}
                  onChange={(value) =>
                    setForm((prev) => ({ ...prev, age: Number(value || 0) }))
                  }
                />
                <Autocomplete
                  label={t("city")}
                  data={russianCities}
                  limit={8}
                  value={form.city}
                  onChange={(event) =>
                    setForm((prev) => ({ ...prev, city: event }))
                  }
                />
              </Group>
              <Group grow>
                <Select
                  label={t("language")}
                  data={[
                    { value: "ru", label: "Русский" },
                    { value: "en", label: "English" }
                  ]}
                  value={form.language}
                  onChange={(value) =>
                    setForm((prev) => ({ ...prev, language: value || "ru" }))
                  }
                />
                <Select
                  label={t("gender")}
                  data={genderOptions}
                  value={form.gender}
                  onChange={(value) =>
                    setForm((prev) => ({ ...prev, gender: (value || "MALE") as "MALE" | "FEMALE" | "OTHER" }))
                  }
                />
              </Group>
              <Textarea
                label={t("about")}
                minRows={3}
                value={form.about}
                onChange={(event) =>
                  setForm((prev) => ({ ...prev, about: event.currentTarget.value }))
                }
              />
              <MultiSelect
                label={t("interests")}
                data={interestOptions}
                value={form.interestCodes}
                onChange={(value) =>
                  setForm((prev) => ({ ...prev, interestCodes: value }))
                }
                placeholder={t("interests")}
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
                placeholder={t("goals")}
                searchable
                maxDropdownHeight={260}
                nothingFoundMessage={t("nothingFound")}
                description={`${t("selected")}: ${form.goalCodes.length}`}
              />
              <Button
                onClick={async () => {
                  try {
                    await onRegister(form);
                  } catch (e) {
                    setError(e instanceof Error ? e.message : "Ошибка");
                  }
                }}
              >
                {t("register")}
              </Button>
            </Stack>
          </Tabs.Panel>
        </Tabs>
      </div>
    </div>
  );
}
