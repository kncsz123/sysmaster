//package priv.cgroup.util;
//
//import org.springframework.context.annotation.Bean;
//
//import javax.persistence.AttributeConverter;
//import javax.persistence.Converter;
//
//@Converter(autoApply = true)
//public class BooleanToTinyintConverter implements AttributeConverter<Boolean, Integer> {
//
//    @Override
//    public Integer convertToDatabaseColumn(Boolean attribute) {
//        if (attribute == null) {
//            return 0;
//        }
//        return attribute ? 1 : 0;
//    }
//
//    @Override
//    public Boolean convertToEntityAttribute(Integer dbData) {
//        if (dbData == null) {
//            return false;
//        }
//        return dbData == 1;
//    }
//}
