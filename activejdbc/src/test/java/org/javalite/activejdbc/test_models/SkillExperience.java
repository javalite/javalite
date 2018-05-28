package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


@Table("tnb_skills")
@CompositePK({"skill_owner", "skill_name"})
    public class SkillExperience extends Model {

    public static boolean add(String identifier, String skill, Integer level, BigInteger experience) {
        SkillExperience skillExperience = (exists(identifier, skill)) ? get(identifier, skill) : new SkillExperience();
        skillExperience.set("skill_owner", identifier);
        skillExperience.set("skill_name", skill);
        skillExperience.set("skill_level", level);
        skillExperience.set("skill_experience", experience.toString());

        return skillExperience.saveIt();
    }

    public static boolean exists(String identifier, String skill) {
        System.out.println("id: " + identifier + " skill: " + skill);
        return SkillExperience.findFirst("skill_owner = ? AND skill_name = ?", identifier, skill) != null;
    }

    public static SkillExperience get(String identifier, String skill) {
        return SkillExperience.findFirst("skill_owner = ? AND skill_name = ?", identifier, skill);
    }

    public static Integer getLevel(String identifier, String skill) {
        if (exists(identifier, skill)) {
            return get(identifier, skill).getInteger("skill_level");
        }
        return 1;
    }

    public static BigInteger getExperience(String identifier, String skill) {
        if (exists(identifier, skill)) {
            return new BigInteger(get(identifier, skill).getString("skill_experience"));
        }
        return BigInteger.ZERO;
    }

    public static Map<String, BigInteger> getSkills(String identifier) {
        Map<String, BigInteger> skills = new HashMap<>();

        SkillExperience.find("skill_owner = ?", identifier).forEach((experience) -> {
            skills.put(experience.getString("skill_name"), new BigInteger(experience.getString("skill_experience")));
        });
        return skills;
    }
}