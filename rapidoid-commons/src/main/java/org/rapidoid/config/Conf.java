package org.rapidoid.config;

import org.rapidoid.scan.ClasspathUtil;
import org.rapidoid.u.U;
import org.rapidoid.util.UTILS;

import java.util.List;

/*
 * #%L
 * rapidoid-commons
 * %%
 * Copyright (C) 2014 - 2016 Nikolche Mihajlovski and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * @author Nikolche Mihajlovski
 * @since 2.0.0
 */
public class Conf {

	public static final Config ROOT = new Config();

	public static final Config OAUTH = section("oauth");
	public static final Config USERS = section("users");
	public static final Config JDBC = section("jdbc");
	public static final Config APP = section("app");
	public static final Config JOBS = section("jobs");
	public static final Config MENU = section("menu");

	static {
		RapidoidInitializer.initialize();
		setPath("");
		autoRefresh(ROOT);
	}

	private static volatile String path = "";

	public static synchronized void args(String... args) {
		ConfigHelp.processHelp(args);
		ROOT.args(args);
	}

	public static boolean micro() {
		return ROOT.is("micro");
	}

	public static boolean production() {
		return ROOT.is("production");
	}

	public static boolean dev() {
		return !production() && !ClasspathUtil.getClasspathFolders().isEmpty();
	}

	public static String secret() {
		return ROOT.entry("secret").str().getOrNull();
	}

	public static void reset() {
		ROOT.clear();
	}

	public static Config section(String name) {
		Config config = ROOT.sub(name);
		autoRefresh(config);
		return config;
	}

	public static Config section(Class<?> clazz) {
		return section(clazz.getSimpleName());
	}

	public static int cpus() {
		return ROOT.entry("cpus").or(Runtime.getRuntime().availableProcessors());
	}

	public static void setPath(String path) {
		Conf.path = path;
		List<List<String>> detached = AutoRefreshingConfig.untrack();
		reset();

		for (List<String> keys : detached) {
			autoRefresh(keys.isEmpty() ? ROOT : ROOT.sub(keys));
		}
	}

	private static void autoRefresh(Config... configs) {
		for (Config config : configs) {
			List<String> keys = config.keys();
			U.must(keys.size() < 2);

			String configName = keys.isEmpty() ? "config" : keys.get(0);
			String filename = UTILS.path(path, configName + ".yaml");

			AutoRefreshingConfig.attach(config, filename);
		}
	}

}
